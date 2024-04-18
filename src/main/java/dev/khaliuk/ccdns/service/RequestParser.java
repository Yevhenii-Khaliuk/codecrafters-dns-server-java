package dev.khaliuk.ccdns.service;

import dev.khaliuk.ccdns.config.Logger;
import dev.khaliuk.ccdns.dto.DnsMessage;
import dev.khaliuk.ccdns.dto.Header;
import dev.khaliuk.ccdns.dto.Question;

import java.util.ArrayList;
import java.util.List;

public final class RequestParser {
    private static final Logger LOGGER = new Logger(RequestParser.class);

    private RequestParser() {
    }

    public static DnsMessage parseRequest(byte[] buffer) {
        Header header = parseHeader(buffer);
        List<Question> questions = parseQuestions(buffer, header.questionCount());

        return DnsMessage.builder()
            .header(header)
            .questions(questions)
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

    /**
     * Question labels might consist of:
     * 1. pointer
     * 2. sequence of labels ending with 0x00 byte
     * 3. sequence of labels ending with pointer
     * So the first {@code if} block targets the case 1;
     * if its condition returns false, the cases 2 and 3 get targeted inside the {@code while} loop;
     * finally, the last {@code if} block targets the last part of the case 3
     */
    private static List<Question> parseQuestions(byte[] buffer, int questionCount) {
        List<Question> questions = new ArrayList<>();
        int currentQuestion = 0;
        int index = 12;

        while (currentQuestion < questionCount) {
            List<String> labels = new ArrayList<>();
            int length = buffer[index] & 0xFF;
            // 1
            if ((length & 192) == 192) { // 192 is a decimal representation of 1100_0000 mask
                // it's a pointer
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
                    index = ((length & 0b0011_1111 & 0xFF) << 8) + (buffer[index + 1] & 0xFF);
                    length = buffer[index] & 0xFF;
                }
            }

            index += 5; // skip the last length byte and type + class fields 2 bytes each

            questions.add(Question.builder()
                .labels(labels)
                .build());

            currentQuestion++;
        }

        return questions;
    }
}
