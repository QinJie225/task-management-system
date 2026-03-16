package org.example.internshipassignmentkafka.service;

import org.example.internshipassignmentkafka.dtos.CreateTaskRequest;
import org.example.internshipassignmentkafka.dtos.TaskResponse;
import org.example.internshipassignmentkafka.dtos.UpdateTaskRequest;
import org.example.internshipassignmentkafka.enums.TaskStatus;

import java.util.List;

public interface TaskService {
    TaskResponse createTask(CreateTaskRequest createTaskRequest);
    List<TaskResponse> getAllTasks();
    TaskResponse getTasks(String taskId);
    void deleteTask(String taskId);
    TaskResponse updateTask(String taskId, UpdateTaskRequest updateTaskRequest);
    List<TaskResponse> getTasksByStatus(TaskStatus status);
}
