package dev.khaliuk.ccdns;

import dev.khaliuk.ccdns.config.Logger;
import dev.khaliuk.ccdns.dto.Answer;
import dev.khaliuk.ccdns.dto.DnsMessage;
import dev.khaliuk.ccdns.dto.Header;
import dev.khaliuk.ccdns.dto.Question;
import dev.khaliuk.ccdns.dto.Type;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final Logger LOGGER = new Logger(Main.class);

    public static void main(String[] args) {
        try (DatagramSocket serverSocket = new DatagramSocket(2053)) {
            while (true) {
                final byte[] buf = new byte[512];
                final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                serverSocket.receive(packet);
                LOGGER.log("Received data");

                DnsMessage request = parseRequest(buf);
                final byte[] response = createResponse(request);
                final DatagramPacket packetResponse =
                    new DatagramPacket(response, response.length, packet.getSocketAddress());
                serverSocket.send(packetResponse);
            }
        } catch (IOException e) {
            LOGGER.log("IOException: " + e.getMessage());
        }
    }

    private static DnsMessage parseRequest(byte[] buffer) {
        return DnsMessage.builder()
            .header(parseHeader(buffer))
            .question(parseQuestion(buffer))
            .build();
    }

    private static Header parseHeader(byte[] buffer) {
        int packetIdentifier = 0;
        packetIdentifier += ((buffer[0] & 0xFF) << 8);
        packetIdentifier += (buffer[1] & 0xFF);

        int operationCode = (buffer[2] & 0x78) >> 3;

        boolean recursionDesired = (buffer[2] & 0x01) == 1;

        int responseCode = operationCode == 0 ? 0 : 4;

        return Header.builder()
            .packetIdentifier(packetIdentifier)
            .operationCode(operationCode)
            .recursionDesired(recursionDesired)
            .responseCode(responseCode)
            .build();
    }

    private static Question parseQuestion(byte[] buffer) {
        List<String> labels = new ArrayList<>();

        int index = 12;
        int length = buffer[index] & 0xFF;
        while (length != 0) {
            byte[] labelBytes = new byte[length];
            System.arraycopy(buffer, index + 1, labelBytes, 0, length);
            labels.add(new String(labelBytes));
            index += length + 1;
            length = buffer[index] & 0xFF;
        }

        return Question.builder()
            .labels(labels)
            .build();
    }

    private static byte[] createResponse(DnsMessage request) {
        return DnsMessage.builder()
            .header(Header.builder()
                .packetIdentifier(request.getHeader().getPacketIdentifier())
                .queryResponse(true)
                .operationCode(request.getHeader().getOperationCode())
                .recursionDesired(request.getHeader().isRecursionDesired())
                .responseCode(request.getHeader().getResponseCode())
                .questionCount(1)
                .answerCount(1)
                .build())
            .question(Question.builder()
                .labels(request.getQuestion().getLabels())
                .type(Type.A)
                .build())
            .answer(Answer.builder()
                .labels(request.getQuestion().getLabels())
                .type(Type.A)
                .ttl(60)
                .length(4)
                .address("8.8.8.8")
                .build())
            .build()
            .serialize();
    }
}
