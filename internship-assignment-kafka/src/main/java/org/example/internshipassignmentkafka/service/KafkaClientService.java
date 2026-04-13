package org.example.internshipassignmentkafka.service;

import org.example.internshipassignmentkafka.dtos.CreateTaskRequest;
import org.example.internshipassignmentkafka.dtos.UpdateTaskRequest;
import reactor.core.publisher.Mono;

public interface KafkaClientService {
    Mono<Void> createTask(CreateTaskRequest request);
    Mono<Void> updateTask(String taskId, UpdateTaskRequest request);
    Mono<Void> deleteTask(String taskId);
}
