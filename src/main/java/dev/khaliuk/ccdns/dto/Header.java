package dev.khaliuk.ccdns.dto;

import dev.khaliuk.ccdns.config.Logger;

import java.io.ByteArrayOutputStream;

/**
 * @param packetIdentifier    16 bits
 * @param queryResponse       1 bit
 * @param operationCode       4 bits
 * @param authoritativeAnswer 1 bit
 * @param truncatedMessage    1 bit
 * @param recursionDesired    1 bit
 * @param recursionAvailable  1 bit
 * @param reserved            3 bits
 * @param responseCode        4 bits
 * @param questionCount       16 bits
 * @param answerCount         16 bits
 * @param authorityCount      16 bits
 * @param additionalCount     16 bits
 */
public record Header(int packetIdentifier,
                     boolean queryResponse,
                     int operationCode,
                     boolean authoritativeAnswer,
                     boolean truncatedMessage,
                     boolean recursionDesired,
                     boolean recursionAvailable,
                     int reserved,
                     int responseCode,
                     int questionCount,
                     int answerCount,
                     int authorityCount,
                     int additionalCount) {

    private static final Logger LOGGER = new Logger(Header.class);

    byte[] serialize() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        buffer.write((packetIdentifier >> 8) & 0xFF);
        buffer.write(packetIdentifier & 0xFF);

        int b = 0;
        if (queryResponse) b += (1 << 7);
        b += ((operationCode & 0x0F) << 3);
        if (authoritativeAnswer) b += (1 << 2);
        if (truncatedMessage) b += (1 << 1);
        if (recursionDesired) b += 1;
        buffer.write(b);

        b = 0;
        if (recursionAvailable) b += (1 << 7);
        b += (responseCode & 0x0F);
        buffer.write(b);

        buffer.write((questionCount >> 8) & 0xFF);
        buffer.write(questionCount & 0xFF);
        buffer.write((answerCount >> 8) & 0xFF);
        buffer.write(answerCount & 0xFF);

        buffer.write((authorityCount >> 8) & 0xFF);
        buffer.write(authorityCount & 0xFF);
        buffer.write((additionalCount >> 8) & 0xFF);
        buffer.write(additionalCount & 0xFF);

        return buffer.toByteArray();
    }

    public static HeaderBuilder builder() {
        return new HeaderBuilder();
    }

    public static class HeaderBuilder {
        private int packetIdentifier;
        private boolean queryResponse;
        private int operationCode;
        private boolean authoritativeAnswer;
        private boolean truncatedMessage;
        private boolean recursionDesired;
        private boolean recursionAvailable;
        private int reserved;
        private int responseCode;
        private int questionCount;
        private int answerCount;
        private int authorityCount;
        private int additionalCount;

        public HeaderBuilder packetIdentifier(int packetIdentifier) {
            this.packetIdentifier = packetIdentifier;
            return this;
        }

        public HeaderBuilder queryResponse(boolean queryResponse) {
            this.queryResponse = queryResponse;
            return this;
        }

        public HeaderBuilder operationCode(int operationCode) {
            this.operationCode = operationCode;
            return this;
        }

        public HeaderBuilder authoritativeAnswer(boolean authoritativeAnswer) {
            this.authoritativeAnswer = authoritativeAnswer;
            return this;
        }

        public HeaderBuilder truncatedMessage(boolean truncatedMessage) {
            this.truncatedMessage = truncatedMessage;
            return this;
        }

        public HeaderBuilder recursionDesired(boolean recursionDesired) {
            this.recursionDesired = recursionDesired;
            return this;
        }

        public HeaderBuilder recursionAvailable(boolean recursionAvailable) {
            this.recursionAvailable = recursionAvailable;
            return this;
        }

        public HeaderBuilder reserved(int reserved) {
            this.reserved = reserved;
            return this;
        }

        public HeaderBuilder responseCode(int responseCode) {
            this.responseCode = responseCode;
            return this;
        }

        public HeaderBuilder questionCount(int questionCount) {
            this.questionCount = questionCount;
            return this;
        }

        public HeaderBuilder answerCount(int answerCount) {
            this.answerCount = answerCount;
            return this;
        }

        public HeaderBuilder authorityCount(int authorityCount) {
            this.authorityCount = authorityCount;
            return this;
        }

        public HeaderBuilder additionalCount(int additionalCount) {
            this.additionalCount = additionalCount;
            return this;
        }

        public Header build() {
            return new Header(this.packetIdentifier, this.queryResponse, this.operationCode, this.authoritativeAnswer,
                this.truncatedMessage, this.recursionDesired, this.recursionAvailable, this.reserved, this.responseCode,
                this.questionCount, this.answerCount, this.authorityCount, this.additionalCount);
        }
    }
}
