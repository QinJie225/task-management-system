package org.example.internshipassignmentkafka.service;

import org.example.internshipassignmentkafka.dtos.CreateTaskRequest;
import org.example.internshipassignmentkafka.dtos.TaskResponse;
import org.example.internshipassignmentkafka.dtos.UpdateTaskRequest;
import org.example.internshipassignmentkafka.enums.TaskStatus;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TaskService {
    Mono<TaskResponse> createTask(CreateTaskRequest request, String taskId, String actorUsername);
    Flux<TaskResponse> getAllTasks(Pageable pageable);
    Mono<TaskResponse> getTask(String taskId);
    Flux<TaskResponse> getTasksByStatus(TaskStatus status);
    Mono<TaskResponse> updateTask(String taskId, UpdateTaskRequest request, String actorUsername);
    Mono<Void> deleteTask(String taskId);
    Mono<Long> countAllTasks();
}
