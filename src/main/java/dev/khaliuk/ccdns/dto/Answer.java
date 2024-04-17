package dev.khaliuk.ccdns.dto;

import dev.khaliuk.ccdns.config.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class Answer {
    private static final Logger LOGGER = new Logger(Answer.class);

    private final List<String> labels; // 1 byte length + label for each of the labels
    private final int type; // 2 bytes
    private final int classCode; // 2 bytes
    private final int ttl; // 4 bytes
    private final int length; // 2 bytes
    // TODO: support other types
    private final InetAddress address; // 4 bytes

    public Answer(List<String> labels, int type, int classCode, int ttl, int length, InetAddress address) {
        this.labels = labels;
        this.type = type;
        this.classCode = classCode;
        this.ttl = ttl;
        this.length = length;
        this.address = address;
    }

    public byte[] serialize() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        labels.forEach(label -> {
            buffer.write(label.length());
            try {
                buffer.write(label.getBytes());
            } catch (IOException e) {
                LOGGER.log("Unexpected error: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });

        buffer.write(0);

        buffer.write((type >> 8) & 0xFF);
        buffer.write(type & 0xFF);

        buffer.write((classCode >> 8) & 0xFF);
        buffer.write(classCode & 0xFF);

        buffer.write((ttl >> 24) & 0xFF);
        buffer.write((ttl >> 16) & 0xFF);
        buffer.write((ttl >> 8) & 0xFF);
        buffer.write(ttl & 0xFF);

        buffer.write((length >> 8) & 0xFF);
        buffer.write(length & 0xFF);

        try {
            buffer.write(address.getAddress());
        } catch (IOException e) {
            LOGGER.log("Unexpected error: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return buffer.toByteArray();
    }

    public static AnswerBuilder builder() {
        return new AnswerBuilder();
    }

    public static class AnswerBuilder {
        private List<String> labels;
        private int type;
        private int classCode = 1;
        private int ttl;
        private int length;
        private InetAddress address;

        public AnswerBuilder labels(List<String> labels) {
            this.labels = labels;
            return this;
        }

        public AnswerBuilder type(int type) {
            this.type = type;
            return this;
        }

        public AnswerBuilder type(Type type) {
            this.type = type.getValue();
            return this;
        }

        public AnswerBuilder classCode(int classCode) {
            this.classCode = classCode;
            return this;
        }

        public AnswerBuilder ttl(int ttl) {
            this.ttl = ttl;
            return this;
        }

        public AnswerBuilder length(int length) {
            this.length = length;
            return this;
        }

        public AnswerBuilder address(InetAddress address) {
            this.address = address;
            return this;
        }

        public AnswerBuilder address(String address) {
            try {
                this.address = InetAddress.getByName(address);
            } catch (UnknownHostException e) {
                LOGGER.log("Error: " + e.getMessage());
                throw new RuntimeException(e);
            }

            return this;
        }

        public Answer build() {
            return new Answer(this.labels, this.type, this.classCode, this.ttl, this.length, this.address);
        }
    }
}
