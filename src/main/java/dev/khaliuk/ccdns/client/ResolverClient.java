package dev.khaliuk.ccdns.client;

import dev.khaliuk.ccdns.config.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ResolverClient {
    private static final Logger LOGGER = new Logger(ResolverClient.class);

    private final InetAddress address;
    private final int port;

    public ResolverClient(String address) throws UnknownHostException {
        String[] hostPort = address.split(":");
        this.address = InetAddress.getByName(hostPort[0]);
        this.port = Integer.parseInt(hostPort[1]);
    }

    public byte[] send(byte[] buf) {
        try (DatagramSocket socket = new DatagramSocket()) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
            socket.send(packet);
            byte[] responseBuf = new byte[512];
            packet = new DatagramPacket(responseBuf, responseBuf.length);
            socket.receive(packet);
            return responseBuf;
        } catch (IOException e) {
            LOGGER.log("Unexpected error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
