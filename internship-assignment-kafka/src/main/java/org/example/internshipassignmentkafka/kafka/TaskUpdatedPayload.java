package org.example.internshipassignmentkafka.kafka;

import org.example.internshipassignmentkafka.dtos.UpdateTaskRequest;

public record TaskUpdatedPayload(
        String taskId,
        UpdateTaskRequest request
) { }
