package dev.khaliuk.ccdns;

import dev.khaliuk.ccdns.config.Logger;
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

                DnsMessage dnsMessage = DnsMessage.builder()
                    .header(Header.builder()
                        .packetIdentifier(1234)
                        .queryResponse(true)
                        .questionCount(1)
                        .build())
                    .question(Question.builder()
                        .labels(List.of("codecrafters", "io"))
                        .type(Type.A)
                        .build())
                    .build();

                final byte[] bufResponse = dnsMessage.serialize();
                final DatagramPacket packetResponse =
                    new DatagramPacket(bufResponse, bufResponse.length, packet.getSocketAddress());
                serverSocket.send(packetResponse);
            }
        } catch (IOException e) {
            LOGGER.log("IOException: " + e.getMessage());
        }
    }


}
