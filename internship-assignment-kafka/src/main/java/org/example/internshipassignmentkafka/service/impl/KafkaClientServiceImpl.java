package org.example.internshipassignmentkafka.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.internshipassignmentkafka.dtos.CreateTaskRequest;
import org.example.internshipassignmentkafka.dtos.UpdateTaskRequest;
import org.example.internshipassignmentkafka.kafka.TaskEventProducer;
import org.example.internshipassignmentkafka.service.KafkaClientService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaClientServiceImpl implements KafkaClientService {

    private final TaskEventProducer taskEventProducer;

    @Override
    public Mono<Void> createTask(CreateTaskRequest request) {
        return taskEventProducer.publishCreateEvent(request)
                .doOnSuccess(v -> log.info("Published TASK_CREATED event for title={}", request.title()));
    }

    @Override
    public Mono<Void> updateTask(String taskId, UpdateTaskRequest request) {
        return taskEventProducer.publishUpdateEvent(taskId, request)
                .doOnSuccess(v -> log.info("Published TASK_UPDATED event for taskId={}", taskId));
    }

    @Override
    public Mono<Void> deleteTask(String taskId) {
        return taskEventProducer.publishDeleteEvent(taskId)
                .doOnSuccess(v -> log.info("Published TASK_DELETED event for taskId={}", taskId));
    }
}