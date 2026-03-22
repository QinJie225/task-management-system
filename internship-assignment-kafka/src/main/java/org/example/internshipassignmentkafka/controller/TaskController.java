package org.example.internshipassignmentkafka.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.internshipassignmentkafka.dtos.CreateTaskRequest;
import org.example.internshipassignmentkafka.dtos.TaskResponse;
import org.example.internshipassignmentkafka.dtos.UpdateTaskRequest;
import org.example.internshipassignmentkafka.enums.TaskStatus;
import org.example.internshipassignmentkafka.kafka.TaskEventProducer;
import org.example.internshipassignmentkafka.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskEventProducer taskEventProducer;
    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<Void> createTask(
            @Valid @RequestBody CreateTaskRequest createTaskRequest
            ) {
        taskEventProducer.publishCreateEvent(createTaskRequest);
        log.info("Published TASK_CREATED event for title={}", createTaskRequest.title());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<Void> updateTask(@PathVariable String taskId, @Valid @RequestBody UpdateTaskRequest updateTaskRequest) {
        taskEventProducer.publishUpdateEvent(taskId, updateTaskRequest);
        log.info("Published TASK_UPDATED event for taskId={}", taskId);
        return ResponseEntity.accepted().build();
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks(
            @RequestParam(required = false)TaskStatus status
            ) {
        if (status != null) {
            return ResponseEntity.ok(taskService.getTasksByStatus(status));
        }

        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTasks(@PathVariable String taskId){
        return ResponseEntity.ok(taskService.getTasks(taskId));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String taskId) {
        taskEventProducer.publishDeleteEvent(taskId);
        log.info("Published TASK_DELETED event for taskId={}", taskId);
        return ResponseEntity.accepted().build();
    }
}
