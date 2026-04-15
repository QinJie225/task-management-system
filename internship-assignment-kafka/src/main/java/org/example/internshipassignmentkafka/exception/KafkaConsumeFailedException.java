package org.example.internshipassignmentkafka.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class KafkaConsumeFailedException extends RuntimeException {
    private final List<String> errors;

    public KafkaConsumeFailedException(String eventType, List<String> errors, Throwable cause) {
        super("Failed to consume Kafka event: " + eventType +
                (errors != null && !errors.isEmpty() ? " - reasons: " + errors : ""), cause);
        this.errors = errors != null ? errors : List.of();
    }
}