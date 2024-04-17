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

                Header requestHeader = parseRequest(buf);

                final byte[] response = createResponse(requestHeader);
                final DatagramPacket packetResponse =
                    new DatagramPacket(response, response.length, packet.getSocketAddress());
                serverSocket.send(packetResponse);
            }
        } catch (IOException e) {
            LOGGER.log("IOException: " + e.getMessage());
        }
    }

    private static Header parseRequest(byte[] buffer) {
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

    private static byte[] createResponse(Header requestHeader) {
        return DnsMessage.builder()
            .header(Header.builder()
                .packetIdentifier(requestHeader.getPacketIdentifier())
                .queryResponse(true)
                .operationCode(requestHeader.getOperationCode())
                .recursionDesired(requestHeader.isRecursionDesired())
                .responseCode(requestHeader.getResponseCode())
                .questionCount(1)
                .answerCount(1)
                .build())
            .question(Question.builder()
                .labels(List.of("codecrafters", "io"))
                .type(Type.A)
                .build())
            .answer(Answer.builder()
                .labels(List.of("codecrafters", "io"))
                .type(Type.A)
                .ttl(60)
                .length(4)
                .address("8.8.8.8")
                .build())
            .build()
            .serialize();
    }
}
