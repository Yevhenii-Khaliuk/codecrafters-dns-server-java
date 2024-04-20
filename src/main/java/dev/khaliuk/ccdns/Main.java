package dev.khaliuk.ccdns;

import dev.khaliuk.ccdns.client.ResolverClient;
import dev.khaliuk.ccdns.config.Logger;
import dev.khaliuk.ccdns.dto.Answer;
import dev.khaliuk.ccdns.dto.DnsMessage;
import dev.khaliuk.ccdns.service.RequestParser;
import dev.khaliuk.ccdns.service.ResponseBuilder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

public class Main {
    private static final Logger LOGGER = new Logger(Main.class);

    private static ResolverClient resolverClient;

    public static void main(String[] args) throws UnknownHostException {
        if (args.length > 1 && args[0].equalsIgnoreCase("--resolver")) {
            resolverClient = new ResolverClient(args[1]);
        }

        try (DatagramSocket serverSocket = new DatagramSocket(2053)) {
            while (true) {
                final byte[] buf = new byte[512];
                final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                serverSocket.receive(packet);
                LOGGER.log("Received data: " + Arrays.toString(packet.getData()));
                DnsMessage request = RequestParser.parseRequest(buf);

                final byte[] response;
                if (resolverClient != null) {
                    LOGGER.log("Forwarding messages to resolver");
                    // send request to resolver and receive response, sending it out to the client
                    List<byte[]> responses = request.questions()
                        .stream()
                        .map(question -> DnsMessage.builder()
                            .header(request.header()
                                .toBuilder()
                                .questionCount(1)
                                .build())
                            .questions(List.of(question))
                            .build()
                            .serialize())
                        .map(message -> resolverClient.send(message))
                        .toList();

                    List<Answer> answers = responses.stream()
                        .map(RequestParser::parseAnswer)
                        .toList();

                    response = ResponseBuilder.createResponse(request, answers);
                } else {
                    response = ResponseBuilder.createResponse(request);
                }

                final DatagramPacket packetResponse =
                    new DatagramPacket(response, response.length, packet.getSocketAddress());
                serverSocket.send(packetResponse);
            }
        } catch (IOException e) {
            LOGGER.log("IOException: " + e.getMessage());
        }
    }
}
