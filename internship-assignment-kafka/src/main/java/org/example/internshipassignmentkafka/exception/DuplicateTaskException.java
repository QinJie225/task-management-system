package org.example.internshipassignmentkafka.exception;

public class DuplicateTaskException extends RuntimeException {
    public DuplicateTaskException(String taskId) {
        super("Task " + taskId + " already exists");
    }
}
