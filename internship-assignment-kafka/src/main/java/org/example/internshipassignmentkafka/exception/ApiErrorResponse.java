package org.example.internshipassignmentkafka.exception;


import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse (
        LocalDateTime timeStamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorDto> fieldErrors
){ }