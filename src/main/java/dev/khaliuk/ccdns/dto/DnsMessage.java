package dev.khaliuk.ccdns.dto;

import dev.khaliuk.ccdns.config.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public record DnsMessage(
    Header header,
    List<Question> questions,
    List<Answer> answers) {

    private static final Logger LOGGER = new Logger(DnsMessage.class);

    public byte[] serialize() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        byte[] headerBuf = header.serialize();
        try {
            buffer.write(headerBuf);
        } catch (IOException e) {
            LOGGER.log("Unexpected error: " + e.getMessage());
            throw new RuntimeException(e);
        }
        questions.forEach(question -> {
            byte[] questionBuf = question.serialize();
            try {
                buffer.write(questionBuf);
            } catch (IOException e) {
                LOGGER.log("Unexpected error: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
        answers.forEach(answer -> {
            byte[] answerBuf = answer.serialize();
            try {
                buffer.write(answerBuf);
            } catch (IOException e) {
                LOGGER.log("Unexpected error: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });

        return buffer.toByteArray();
    }

    public static DnsMessageBuilder builder() {
        return new DnsMessageBuilder();
    }

    public static class DnsMessageBuilder {
        private Header header;
        private List<Question> questions;
        private List<Answer> answers = List.of();

        public DnsMessageBuilder header(Header header) {
            this.header = header;
            return this;
        }

        public DnsMessageBuilder questions(List<Question> questions) {
            this.questions = questions;
            return this;
        }

        public DnsMessageBuilder answers(List<Answer> answers) {
            this.answers = answers;
            return this;
        }

        public DnsMessage build() {
            return new DnsMessage(this.header, this.questions, this.answers);
        }
    }
}
