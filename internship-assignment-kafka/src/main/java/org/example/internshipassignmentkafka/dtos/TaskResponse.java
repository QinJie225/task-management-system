package org.example.internshipassignmentkafka.dtos;

import org.example.internshipassignmentkafka.enums.TaskPriority;
import org.example.internshipassignmentkafka.enums.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
        String id,
        String taskId,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String createdBy,
        String updatedBy
) { }
