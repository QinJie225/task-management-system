package org.example.internshipassignmentkafka.kafka;

import java.time.LocalDateTime;

public record WebhookPayload(
        String eventType,
        String taskId,
        String status,
        String message,
        LocalDateTime processedAt
) { }
