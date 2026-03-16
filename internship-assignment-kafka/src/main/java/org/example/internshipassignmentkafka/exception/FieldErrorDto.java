package org.example.internshipassignmentkafka.exception;

public record FieldErrorDto (
        String field,
        String message
){ }
