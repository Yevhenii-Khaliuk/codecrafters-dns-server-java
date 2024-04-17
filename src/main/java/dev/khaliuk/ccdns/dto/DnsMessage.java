package dev.khaliuk.ccdns.dto;

import dev.khaliuk.ccdns.config.Logger;

public class DnsMessage {
    private static final Logger LOGGER = new Logger(DnsMessage.class);

    private final Header header;
    private final Question question; // TODO: support a list of questions
    private final Answer answer; // TODO: support a list of answers

    public DnsMessage(Header header, Question question, Answer answer) {
        this.header = header;
        this.question = question;
        this.answer = answer;
    }

    public byte[] serialize() {
        byte[] buf = new byte[512];

        byte[] headerBuf = header.serialize();
        System.arraycopy(headerBuf, 0, buf, 0, headerBuf.length);
        byte[] questionBuf = question.serialize();
        System.arraycopy(questionBuf, 0, buf, headerBuf.length, questionBuf.length);
        byte[] answerBuf = answer.serialize();
        System.arraycopy(answerBuf, 0, buf, headerBuf.length + questionBuf.length, answerBuf.length);

        return buf;
    }

    public static DnsMessageBuilder builder() {
        return new DnsMessageBuilder();
    }

    public static class DnsMessageBuilder {
        private Header header;
        private Question question;
        private Answer answer;

        public DnsMessageBuilder header(Header header) {
            this.header = header;
            return this;
        }

        public DnsMessageBuilder question(Question question) {
            this.question = question;
            return this;
        }

        public DnsMessageBuilder answer(Answer answer) {
            this.answer = answer;
            return this;
        }

        public DnsMessage build() {
            return new DnsMessage(this.header, this.question, this.answer);
        }
    }
}
