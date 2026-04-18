package org.example.internshipassignmentkafka.dtos;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.example.internshipassignmentkafka.enums.TaskPriority;
import org.example.internshipassignmentkafka.enums.TaskStatus;
import org.example.internshipassignmentkafka.utility.ValidationMessages;

import java.time.LocalDate;

public record UpdateTaskRequest(
        @Pattern(regexp = "^(?!\\s*$).+", message = ValidationMessages.TITLE_NOT_BLANK)
        @Size(min = 3, max = 35, message = ValidationMessages.TITLE_SIZE)
        String title,
        @Pattern(regexp = "^(?!\\s*$).+", message = ValidationMessages.DESCRIPTION_NOT_BLANK)
        @Size(min = 10, max = 500, message = ValidationMessages.DESCRIPTION_SIZE)
        String description,
        TaskStatus status,
        TaskPriority priority,
        @FutureOrPresent(message = ValidationMessages.DUE_DATE_FUTURE_OR_PRESENT)
        LocalDate dueDate
) { }
