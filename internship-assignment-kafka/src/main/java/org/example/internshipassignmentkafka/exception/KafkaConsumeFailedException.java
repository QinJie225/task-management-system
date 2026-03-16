package org.example.internshipassignmentkafka.exception;

public class KafkaConsumeFailedException extends RuntimeException {
    public KafkaConsumeFailedException(String eventType, String taskId, Throwable cause) {
        super("Failed to consume Kafka event: " + eventType + " for " + taskId, cause);
    }
}