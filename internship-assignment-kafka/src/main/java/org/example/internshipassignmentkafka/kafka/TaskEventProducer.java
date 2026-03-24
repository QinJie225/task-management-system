package org.example.internshipassignmentkafka.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.internshipassignmentkafka.dtos.CreateTaskRequest;
import org.example.internshipassignmentkafka.dtos.UpdateTaskRequest;
import org.example.internshipassignmentkafka.exception.KafkaPublishFailedException;
import org.example.internshipassignmentkafka.service.SequenceGeneratorService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskEventProducer {

    private final KafkaTemplate<String, TaskEvent> kafkaTemplate;
    private final SequenceGeneratorService sequenceGeneratorService;

    private static final String TOPIC = "task-events";

    public Mono<Void> publishCreateEvent(CreateTaskRequest request) {
        return sequenceGeneratorService.generateSequence("task_sequence")
                .map(nextId -> String.format("TASK-%03d", nextId))
                .flatMap(taskId -> {
                    CreateTaskPayload payload = new CreateTaskPayload(taskId, request);
                    TaskEvent event = new TaskEvent("TASK_CREATED", LocalDateTime.now(), payload);
                    return send(taskId, event);
                });
    }

    public Mono<Void> publishUpdateEvent(String taskId, UpdateTaskRequest request) {
        TaskUpdatedPayload payload = new TaskUpdatedPayload(taskId, request);
        TaskEvent event = new TaskEvent("TASK_UPDATED", LocalDateTime.now(), payload);
        return send(taskId, event);
    }

    public Mono<Void> publishDeleteEvent(String taskId) {
        TaskEvent event = new TaskEvent("TASK_DELETED", LocalDateTime.now(), taskId);
        return send(taskId, event);
    }

    private Mono<Void> send(String key, TaskEvent event) {
        return Mono.fromFuture(
                        kafkaTemplate.send(TOPIC, key, event).toCompletableFuture()
                )
                .doOnSuccess(result -> log.info("Published Kafka Event: {} — offset: {}",
                        event.getEventType(),
                        result.getRecordMetadata().offset()))
                .doOnError(ex -> log.error("Failed to publish Kafka Event: {} - {}",
                        event.getEventType(), ex.getMessage(), ex))
                .onErrorMap(ex -> new KafkaPublishFailedException(event.getEventType(), key, ex))
                .then();
    }
}