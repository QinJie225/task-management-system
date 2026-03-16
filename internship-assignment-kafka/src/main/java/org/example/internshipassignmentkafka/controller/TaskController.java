package org.example.internshipassignmentkafka.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.internshipassignmentkafka.dtos.CreateTaskRequest;
import org.example.internshipassignmentkafka.dtos.TaskResponse;
import org.example.internshipassignmentkafka.dtos.UpdateTaskRequest;
import org.example.internshipassignmentkafka.enums.TaskStatus;
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
    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest createTaskRequest
            ) {
        TaskResponse taskResponse = taskService.createTask(createTaskRequest);
        log.info(
                "Task is created successfully. taskId={}, title={}, status={}, createdDate={}",
                taskResponse.taskId(),
                taskResponse.title(),
                taskResponse.status(),
                taskResponse.createdAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(taskResponse);
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable String taskId, @Valid @RequestBody UpdateTaskRequest updateTaskRequest) {
        TaskResponse response = taskService.updateTask(taskId, updateTaskRequest);
        log.info("Task with taskId {} and title {} was updated successfully", response.taskId(), response.title());
        return ResponseEntity.ok(response);
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
        taskService.deleteTask(taskId);
        log.info("Task with taskId {} was deleted successfully", taskId);
        return ResponseEntity.noContent().build();
    }
}
