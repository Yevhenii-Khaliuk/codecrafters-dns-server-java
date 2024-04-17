package dev.khaliuk.ccdns.dto;

import dev.khaliuk.ccdns.config.Logger;

public class DnsMessage {
    private static final Logger LOGGER = new Logger(DnsMessage.class);

    private final Header header;

    public DnsMessage(Header header) {
        this.header = header;
    }

    public byte[] serialize() {
        byte[] buf = new byte[512];

        byte[] headerBuf = header.serialize();
        System.arraycopy(headerBuf, 0, buf, 0, headerBuf.length);

        return buf;
    }

    public static DnsMessageBuilder builder() {
        return new DnsMessageBuilder();
    }

    public static class DnsMessageBuilder {
        private Header header;

        public DnsMessageBuilder header(Header header) {
            this.header = header;
            return this;
        }

        public DnsMessage build() {
            return new DnsMessage(this.header);
        }
    }
}
