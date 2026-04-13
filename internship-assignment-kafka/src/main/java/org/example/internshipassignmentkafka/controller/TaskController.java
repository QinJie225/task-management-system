package org.example.internshipassignmentkafka.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.internshipassignmentkafka.dtos.CreateTaskRequest;
import org.example.internshipassignmentkafka.dtos.TaskResponse;
import org.example.internshipassignmentkafka.dtos.UpdateTaskRequest;
import org.example.internshipassignmentkafka.enums.TaskStatus;
import org.example.internshipassignmentkafka.exception.EmptyUpdateRequestException;
import org.example.internshipassignmentkafka.kafka.TaskEvent;
import org.example.internshipassignmentkafka.kafka.TaskEventProducer;
import org.example.internshipassignmentkafka.service.KafkaClientService;
import org.example.internshipassignmentkafka.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final KafkaClientService kafkaClientService;
    private final TaskService taskService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> createTask(@Valid @RequestBody CreateTaskRequest createTaskRequest) {
        return kafkaClientService.createTask(createTaskRequest);
    }

    @PatchMapping("/{taskId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> updateTask(
            @PathVariable String taskId,
            @Valid @RequestBody UpdateTaskRequest updateTaskRequest) {

        if (updateTaskRequest.title() == null &&
                updateTaskRequest.description() == null &&
                updateTaskRequest.status() == null &&
                updateTaskRequest.priority() == null &&
                updateTaskRequest.dueDate() == null
        ) {
            return Mono.error(new EmptyUpdateRequestException());
        }

        return kafkaClientService.updateTask(taskId, updateTaskRequest);
    }

    @GetMapping
    public Flux<TaskResponse> getAllTasks(@RequestParam(required = false) TaskStatus status) {
        if (status != null) {
            return taskService.getTasksByStatus(status);
        }
        return taskService.getAllTasks();
    }

    @GetMapping("/{taskId}")
    public Mono<TaskResponse> getTask(@PathVariable String taskId) {
        return taskService.getTask(taskId);
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> deleteTask(@PathVariable String taskId) {
        return kafkaClientService.deleteTask(taskId);
    }

    // Testing Purpose
    private final TaskEventProducer taskEventProducer;

    @PostMapping("/test/publish-raw")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> publishRaw(@RequestBody TaskEvent event) {
        return taskEventProducer.publishRawEvent(event);
    }
}
