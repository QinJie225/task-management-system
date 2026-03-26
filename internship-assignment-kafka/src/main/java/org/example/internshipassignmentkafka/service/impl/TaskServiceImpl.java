package org.example.internshipassignmentkafka.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.internshipassignmentkafka.dtos.CreateTaskRequest;
import org.example.internshipassignmentkafka.dtos.TaskResponse;
import org.example.internshipassignmentkafka.dtos.UpdateTaskRequest;
import org.example.internshipassignmentkafka.enums.TaskStatus;
import org.example.internshipassignmentkafka.exception.DuplicateTaskException;
import org.example.internshipassignmentkafka.exception.TaskNotFoundException;
import org.example.internshipassignmentkafka.mapper.TaskMapper;
import org.example.internshipassignmentkafka.model.Task;
import org.example.internshipassignmentkafka.repository.TaskRepository;
import org.example.internshipassignmentkafka.service.TaskService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    @Override
    public Mono<TaskResponse> createTask(CreateTaskRequest createTaskRequest, String taskId) {
        return taskRepository.findByTaskId(taskId)
                .flatMap(existing -> Mono.<Task>error(new DuplicateTaskException(taskId)))
                .switchIfEmpty(
                        Mono.defer(() -> {
                            Task task = taskMapper.toEntity(createTaskRequest);
                            task.setTaskId(taskId);
                            task.setStatus(TaskStatus.PENDING);
                            return taskRepository.save(task);
                        })
                )
                .map(taskMapper::toDto);
    }

    @Override
    public Flux<TaskResponse> getAllTasks() {
        return taskRepository.findAll()
                .map(taskMapper::toDto);
    }

    @Override
    public Mono<TaskResponse> getTask(String taskId) {
        return taskRepository.findByTaskId(taskId)
                .switchIfEmpty(Mono.error(new TaskNotFoundException(taskId)))
                .map(taskMapper::toDto);
    }

    @Override
    public Flux<TaskResponse> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status)
                .map(taskMapper::toDto);
    }

    @Override
    public Mono<Void> deleteTask(String taskId) {
        return taskRepository.findByTaskId(taskId)
                .switchIfEmpty(Mono.error(new TaskNotFoundException(taskId)))
                .flatMap(taskRepository::delete);
    }

    @Override
    public Mono<TaskResponse> updateTask(String taskId, UpdateTaskRequest request) {
        return taskRepository.findByTaskId(taskId)
                .switchIfEmpty(Mono.error(new TaskNotFoundException(taskId)))
                .flatMap(task -> {
                    taskMapper.updateTask(request, task);
                    return taskRepository.save(task);
                })
                .map(taskMapper::toDto);
    }

//    @Override
//    public Mono<Boolean> existsTaskByTaskId(String taskId) {
//        return taskRepository.findByTaskId(taskId)
//                .hasElement();
//    }
}
