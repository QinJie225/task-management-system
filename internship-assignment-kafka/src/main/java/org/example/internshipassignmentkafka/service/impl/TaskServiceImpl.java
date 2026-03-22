package org.example.internshipassignmentkafka.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.internshipassignmentkafka.dtos.CreateTaskRequest;
import org.example.internshipassignmentkafka.dtos.TaskResponse;
import org.example.internshipassignmentkafka.dtos.UpdateTaskRequest;
import org.example.internshipassignmentkafka.enums.TaskStatus;
import org.example.internshipassignmentkafka.exception.EmptyUpdateRequestException;
import org.example.internshipassignmentkafka.exception.TaskNotFoundException;
import org.example.internshipassignmentkafka.kafka.TaskEventProducer;
import org.example.internshipassignmentkafka.mapper.TaskMapper;
import org.example.internshipassignmentkafka.model.Task;
import org.example.internshipassignmentkafka.repository.TaskRepository;
import org.example.internshipassignmentkafka.service.TaskService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final TaskEventProducer taskEventProducer;

    @Override
    public TaskResponse createTask(CreateTaskRequest createTaskRequest, String taskId) {
        Task task = taskMapper.toEntity(createTaskRequest);
        task.setTaskId(taskId);
        task.setStatus(TaskStatus.PENDING);
        return taskMapper.toDto(taskRepository.save(task));
    }

    @Override
    public List<TaskResponse> getAllTasks() {
        List<Task> taskList = taskRepository.findAll();
        return taskList.stream().map(taskMapper::toDto).toList();
    }

    @Override
    public TaskResponse getTasks(String taskId) {
        Task task = taskRepository.findByTaskId(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
        return taskMapper.toDto(task);
    }

    @Override
    public List<TaskResponse> getTasksByStatus(TaskStatus status) {

        List<Task> tasks = taskRepository.findByStatus(status);

        return tasks.stream()
                .map(taskMapper::toDto)
                .toList();
    }

    @Override
    public void deleteTask(String taskId) {
        Task task = taskRepository.findByTaskId(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
        taskRepository.delete(task);
    }

    @Override
    public TaskResponse updateTask(String taskId, UpdateTaskRequest updateTaskRequest) {

        Task task = taskRepository.findByTaskId(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        if (isEmptyUpdate(updateTaskRequest)) {
            throw new EmptyUpdateRequestException();
        }

        taskMapper.updateTask(updateTaskRequest, task);
        Task updatedTask = taskRepository.save(task);

        return taskMapper.toDto(updatedTask);
    }


    private boolean isEmptyUpdate(UpdateTaskRequest request) {
        return request.title() == null &&
                request.description() == null &&
                request.status() == null &&
                request.priority() == null &&
                request.dueDate() == null;
    }

    @Override
    public boolean exists(String taskId) {
        return taskRepository.findByTaskId(taskId).isPresent();
    }
}
