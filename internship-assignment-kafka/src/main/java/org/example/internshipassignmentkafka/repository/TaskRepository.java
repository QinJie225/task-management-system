package org.example.internshipassignmentkafka.repository;

import org.example.internshipassignmentkafka.enums.TaskStatus;
import org.example.internshipassignmentkafka.model.Task;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TaskRepository extends ReactiveMongoRepository<Task, String> {
    Mono<Task> findByTaskId(String taskId);

    Flux<Task> findByStatus(TaskStatus status);
}
