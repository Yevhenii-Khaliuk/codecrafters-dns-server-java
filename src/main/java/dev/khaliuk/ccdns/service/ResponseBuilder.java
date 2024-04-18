package dev.khaliuk.ccdns.service;

import dev.khaliuk.ccdns.dto.Answer;
import dev.khaliuk.ccdns.dto.DnsMessage;
import dev.khaliuk.ccdns.dto.Header;
import dev.khaliuk.ccdns.dto.Question;
import dev.khaliuk.ccdns.dto.Type;

import java.util.List;

public final class ResponseBuilder {
    private ResponseBuilder() {
    }

    public static byte[] createResponse(DnsMessage request) {
        List<Question> questions = request.questions()
            .stream()
            .map(q -> Question.builder()
                .labels(q.labels())
                .type(Type.A)
                .build())
            .toList();
        List<Answer> answers = request.questions()
            .stream()
            .map(q -> Answer.builder()
                .labels(q.labels())
                .type(Type.A)
                .ttl(60)
                .length(4)
                .address("8.8.8.8")
                .build())
            .toList();

        return DnsMessage.builder()
            .header(Header.builder()
                .packetIdentifier(request.header().packetIdentifier())
                .queryResponse(true)
                .operationCode(request.header().operationCode())
                .recursionDesired(request.header().recursionDesired())
                .responseCode(request.header().responseCode())
                .questionCount(request.header().questionCount())
                .answerCount(request.header().questionCount())
                .build())
            .questions(questions)
            .answers(answers)
            .build()
            .serialize();
    }
}
