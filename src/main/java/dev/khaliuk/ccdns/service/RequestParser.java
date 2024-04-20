package dev.khaliuk.ccdns.service;

import dev.khaliuk.ccdns.config.Logger;
import dev.khaliuk.ccdns.dto.Answer;
import dev.khaliuk.ccdns.dto.DnsMessage;
import dev.khaliuk.ccdns.dto.Header;
import dev.khaliuk.ccdns.dto.Question;
import dev.khaliuk.ccdns.dto.Type;
import org.apache.commons.lang3.tuple.Pair;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class RequestParser {
    private static final Logger LOGGER = new Logger(RequestParser.class);

    private RequestParser() {
    }

    public static DnsMessage parseRequest(byte[] buffer) {
        Header header = parseHeader(buffer);
        List<Question> questions = parseQuestions(buffer, header.questionCount()).getLeft();

        return DnsMessage.builder()
            .header(header)
            .questions(questions)
            .build();
    }

    public static Answer parseAnswer(byte[] buffer) {
        Pair<List<Question>, Integer> questionIndexPair = parseQuestions(buffer, 1);

        // answer record starts with preamble, first part of which has the same structure as a question,
        // followed by a 4-byte TTL, a 2-byte length and for Type.A record a 4 byte Integer
        int recordIndex = parseQuestions(buffer, 2, questionIndexPair.getRight()).getRight() + 6;
        byte[] addressRecord = new byte[4];
        System.arraycopy(buffer, recordIndex, addressRecord, 0, addressRecord.length);

        InetAddress address;
        try {
            address = InetAddress.getByAddress(addressRecord);
        } catch (UnknownHostException e) {
            LOGGER.log("Illegal address: " + Arrays.toString(addressRecord));
            throw new RuntimeException(e);
        }

        Question question = questionIndexPair.getLeft().getFirst();

        return Answer.builder()
            .labels(question.labels())
            .type(question.type())
            .ttl(parseTtl(buffer, questionIndexPair.getRight()))
            .length(4)
            .address(address.getHostAddress())
            .build();
    }

    private static Header parseHeader(byte[] buffer) {
        int packetIdentifier = 0;
        packetIdentifier += ((buffer[0] & 0xFF) << 8);
        packetIdentifier += (buffer[1] & 0xFF);

        int operationCode = (buffer[2] & 0x78) >> 3;

        boolean recursionDesired = (buffer[2] & 0x01) == 1;

        int responseCode = operationCode == 0 ? 0 : 4;

        int questionCount = 0;
        questionCount += ((buffer[4] & 0xFF) << 8);
        questionCount += (buffer[5] & 0xFF);

        return Header.builder()
            .packetIdentifier(packetIdentifier)
            .operationCode(operationCode)
            .recursionDesired(recursionDesired)
            .responseCode(responseCode)
            .questionCount(questionCount)
            .build();
    }

    private static Pair<List<Question>, Integer> parseQuestions(byte[] buffer, int questionCount) {
        return parseQuestions(buffer, questionCount, 12);
    }

    /**
     * Question labels might consist of:
     * 1. pointer
     * 2. sequence of labels ending with 0x00 byte
     * 3. sequence of labels ending with pointer
     * So the first {@code if} block targets the case 1;
     * if its condition returns false, the cases 2 and 3 get targeted inside the {@code while} loop;
     * finally, the last {@code if} block targets the last part of the case 3
     *
     * @return a list of parsed questions and next index
     */
    private static Pair<List<Question>, Integer> parseQuestions(byte[] buffer, int questionCount, int index) {
        List<Question> questions = new ArrayList<>();
        int currentQuestion = 0;
        int lastLabelsIndex = 0;

        while (currentQuestion < questionCount) {
            List<String> labels = new ArrayList<>();
            int length = buffer[index] & 0xFF;
            // 1
            if ((length & 192) == 192) { // 192 is a decimal representation of 1100_0000 mask
                // it's a pointer
                lastLabelsIndex = index + 1;
                index = ((length & 0b0011_1111 & 0xFF) << 8) + (buffer[index + 1] & 0xFF);
                length = buffer[index] & 0xFF;
            }
            // 2
            while (length != 0) {
                byte[] labelBytes = new byte[length];
                System.arraycopy(buffer, index + 1, labelBytes, 0, length);
                labels.add(new String(labelBytes));
                index += length + 1;
                length = buffer[index] & 0xFF;
                // 3
                if ((length & 192) == 192) { // 192 is a decimal representation of 1100_0000 mask
                    // it's a pointer
                    lastLabelsIndex = index + 1;
                    index = ((length & 0b0011_1111 & 0xFF) << 8) + (buffer[index + 1] & 0xFF);
                    length = buffer[index] & 0xFF;
                }
            }

            if (lastLabelsIndex == 0) {
                lastLabelsIndex = index;
            }

            index += 5; // skip the last length byte and type + class fields 2 bytes each

            questions.add(Question.builder()
                .labels(labels)
                .type(Type.A)
                .build());

            currentQuestion++;
        }

        return Pair.of(questions, lastLabelsIndex + 5);
    }

    private static int parseTtl(byte[] buffer, int index) {
        int ttl = 0;
        ttl += ((buffer[index] & 0xFF) << 24);
        ttl += ((buffer[index + 1] & 0xFF) << 16);
        ttl += ((buffer[index + 2] & 0xFF) << 8);
        ttl += (buffer[index + 3] & 0xFF);
        return ttl;
    }
}
