package org.example.internshipassignmentkafka.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.internshipassignmentkafka.dtos.CreateTaskRequest;
import org.example.internshipassignmentkafka.dtos.PaginatedResponse;
import org.example.internshipassignmentkafka.dtos.TaskResponse;
import org.example.internshipassignmentkafka.dtos.UpdateTaskRequest;
import org.example.internshipassignmentkafka.enums.TaskStatus;
import org.example.internshipassignmentkafka.exception.EmptyUpdateRequestException;
import org.example.internshipassignmentkafka.kafka.TaskEvent;
import org.example.internshipassignmentkafka.kafka.TaskEventProducer;
import org.example.internshipassignmentkafka.service.KafkaClientService;
import org.example.internshipassignmentkafka.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


@Slf4j
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
    public Mono<PaginatedResponse<TaskResponse>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction)
    {
        Pageable pageable = PageRequest.of(page, size,
                direction.equals("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending()
        );

        return Mono.zip(
                taskService.getAllTasks(pageable).collectList(),
                taskService.countAllTasks()
        ).map(tuple -> {
            List<TaskResponse> tasks = tuple.getT1();
            long total = tuple.getT2();
            int totalPages = (int) Math.ceil((double) total / size);

            return new PaginatedResponse<>(
                    tasks,
                    total,
                    totalPages,
                    page,
                    size,
                    page == 0,
                    page >= totalPages - 1
            );
        });
    }

//    @GetMapping
//    public Flux<TaskResponse> getAllTasks(
//            @RequestParam(required = false) TaskStatus status) {
//        if (status != null) {
//            return taskService.getTasksByStatus(status);
//        }
//        return taskService.getAllTasks();
//    }

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
