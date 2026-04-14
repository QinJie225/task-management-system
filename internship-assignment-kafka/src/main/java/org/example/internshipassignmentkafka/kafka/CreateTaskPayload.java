package org.example.internshipassignmentkafka.kafka;

import org.example.internshipassignmentkafka.dtos.CreateTaskRequest;

public record CreateTaskPayload(
        String taskId,
        CreateTaskRequest request,
        String actorUsername
) { }
