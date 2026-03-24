package org.example.internshipassignmentkafka.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.internshipassignmentkafka.dtos.CreateTaskRequest;
import org.example.internshipassignmentkafka.dtos.TaskResponse;
import org.example.internshipassignmentkafka.dtos.UpdateTaskRequest;
import org.example.internshipassignmentkafka.enums.TaskStatus;
import org.example.internshipassignmentkafka.exception.EmptyUpdateRequestException;
import org.example.internshipassignmentkafka.kafka.TaskEventProducer;
import org.example.internshipassignmentkafka.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskEventProducer taskEventProducer;
    private final TaskService taskService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> createTask(@Valid @RequestBody CreateTaskRequest createTaskRequest) {
        return Mono.defer(() -> taskEventProducer.publishCreateEvent(createTaskRequest))
                .doOnSuccess(v -> log.info("Published TASK_CREATED event for title={}", createTaskRequest.title()))
                .then();
    }

    @PatchMapping("/{taskId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> updateTask(
            @PathVariable String taskId,
            @Valid @RequestBody UpdateTaskRequest updateTaskRequest) {

        if (taskService.isEmptyUpdate(updateTaskRequest)) {
            return Mono.error(new EmptyUpdateRequestException());
        }

        return Mono.defer(() ->
                        taskEventProducer.publishUpdateEvent(taskId, updateTaskRequest)
                )
                .doOnSuccess(v -> log.info("Published TASK_UPDATED event for taskId={}", taskId))
                .then();
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
        return Mono.defer(() -> taskEventProducer.publishDeleteEvent(taskId))
                .doOnSuccess(v -> log.info("Published TASK_DELETED event for taskId={}", taskId))
                .then();
    }
}
