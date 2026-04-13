package org.example.internshipassignmentkafka.service;

import org.example.internshipassignmentkafka.dtos.CreateTaskRequest;
import org.example.internshipassignmentkafka.dtos.TaskResponse;
import org.example.internshipassignmentkafka.dtos.UpdateTaskRequest;
import org.example.internshipassignmentkafka.enums.TaskStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TaskService {
    Mono<TaskResponse> createTask(CreateTaskRequest request, String taskId);
    Flux<TaskResponse> getAllTasks();
    Mono<TaskResponse> getTask(String taskId);
    Flux<TaskResponse> getTasksByStatus(TaskStatus status);
    Mono<TaskResponse> updateTask(String taskId, UpdateTaskRequest request);
    Mono<Void> deleteTask(String taskId);
//    Mono<Boolean> existsTaskByTaskId(String taskId);
}
