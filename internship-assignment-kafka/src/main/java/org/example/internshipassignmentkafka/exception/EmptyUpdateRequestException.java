package org.example.internshipassignmentkafka.exception;

public class EmptyUpdateRequestException extends RuntimeException {
    public EmptyUpdateRequestException() {
        super("Empty update request");
    }
}
