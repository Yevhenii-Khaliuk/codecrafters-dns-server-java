package dev.khaliuk.ccdns.dto;

import dev.khaliuk.ccdns.config.Logger;

public class DnsMessage {
    private static final Logger LOGGER = new Logger(DnsMessage.class);

    private final Header header;
    private final Question question; // TODO: support a list of questions

    public DnsMessage(Header header, Question question) {
        this.header = header;
        this.question = question;
    }

    public byte[] serialize() {
        byte[] buf = new byte[512];

        byte[] headerBuf = header.serialize();
        System.arraycopy(headerBuf, 0, buf, 0, headerBuf.length);
        byte[] questionBuf = question.serialize();
        System.arraycopy(questionBuf, 0, buf, headerBuf.length, questionBuf.length);

        return buf;
    }

    public static DnsMessageBuilder builder() {
        return new DnsMessageBuilder();
    }

    public static class DnsMessageBuilder {
        private Header header;
        private Question question;

        public DnsMessageBuilder header(Header header) {
            this.header = header;
            return this;
        }

        public DnsMessageBuilder question(Question question) {
            this.question = question;
            return this;
        }

        public DnsMessage build() {
            return new DnsMessage(this.header, this.question);
        }
    }
}
