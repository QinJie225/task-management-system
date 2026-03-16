package org.example.internshipassignmentkafka.exception;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String taskId) {
        super("Task " + taskId + " is not found");
    }
}
