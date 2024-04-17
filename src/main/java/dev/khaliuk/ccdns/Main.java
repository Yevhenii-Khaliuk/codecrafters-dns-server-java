package dev.khaliuk.ccdns;

import dev.khaliuk.ccdns.config.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Main {
    private static final Logger LOGGER = new Logger(Main.class);

    public static void main(String[] args) {
        try (DatagramSocket serverSocket = new DatagramSocket(2053)) {
            while (true) {
                final byte[] buf = new byte[512];
                final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                serverSocket.receive(packet);
                LOGGER.log("Received data");

                final byte[] bufResponse = new byte[512];
                final DatagramPacket packetResponse =
                    new DatagramPacket(bufResponse, bufResponse.length, packet.getSocketAddress());
                serverSocket.send(packetResponse);
            }
        } catch (IOException e) {
            LOGGER.log("IOException: " + e.getMessage());
        }
    }
}
