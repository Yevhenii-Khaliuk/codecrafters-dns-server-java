package dev.khaliuk.ccdns.dto;

import dev.khaliuk.ccdns.config.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * @param labels    1 byte length + label for each of the labels
 * @param type      2 bytes
 * @param classCode 2 bytes, most of the time it's '1', so builder has it as default
 */
public record Question(
    List<String> labels,
    int type,
    int classCode) {

    private static final Logger LOGGER = new Logger(Question.class);

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

        return buffer.toByteArray();
    }

    public static QuestionBuilder builder() {
        return new QuestionBuilder();
    }

    public static class QuestionBuilder {
        private List<String> labels;
        private int type;
        private int classCode = 1;

        public QuestionBuilder labels(List<String> labels) {
            this.labels = labels;
            return this;
        }

        public QuestionBuilder type(int type) {
            this.type = type;
            return this;
        }

        public QuestionBuilder type(Type type) {
            this.type = type.getValue();
            return this;
        }

        public QuestionBuilder classCode(int classCode) {
            this.classCode = classCode;
            return this;
        }

        public Question build() {
            return new Question(labels, type, classCode);
        }
    }
}
