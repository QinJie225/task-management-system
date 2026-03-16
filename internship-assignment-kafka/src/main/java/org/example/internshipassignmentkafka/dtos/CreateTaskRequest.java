package org.example.internshipassignmentkafka.dtos;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.internshipassignmentkafka.enums.TaskPriority;
import org.example.internshipassignmentkafka.utility.ValidationMessages;

import java.time.LocalDate;

public record CreateTaskRequest(
    @NotBlank(message = ValidationMessages.TITLE_NOT_BLANK)
    @Size(min = 3, max = 35, message = ValidationMessages.TITLE_SIZE)
    String title,
    @NotBlank(message = ValidationMessages.DESCRIPTION_NOT_BLANK)
    @Size(min = 3, max = 100, message = ValidationMessages.DESCRIPTION_SIZE)
    String description,
    @NotNull(message = ValidationMessages.PRIORITY_NOT_NULL)
    TaskPriority priority,
    @NotNull(message = ValidationMessages.DUE_DATE_NOT_NULL)
    @FutureOrPresent(message = ValidationMessages.DUE_DATE_FUTURE_OR_PRESENT)
    LocalDate dueDate
) { }
