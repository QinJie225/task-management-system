package org.example.internshipassignmentkafka.repository;

import org.example.internshipassignmentkafka.enums.TaskStatus;
import org.example.internshipassignmentkafka.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends MongoRepository<Task, String> {
    Optional<Task> findByTaskId(String taskId);

    List<Task> findByStatus(TaskStatus status);
}
