package org.example.internshipassignmentkafka.exception;

public class KafkaPublishFailedException extends RuntimeException {
    public KafkaPublishFailedException(String eventType, String taskId, Throwable cause) {
        super("Failed to publish Kafka event: " + eventType + " for " + taskId, cause);
    }
}