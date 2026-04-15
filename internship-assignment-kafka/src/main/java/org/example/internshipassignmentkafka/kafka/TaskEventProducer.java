package org.example.internshipassignmentkafka.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.internshipassignmentkafka.dtos.CreateTaskRequest;
import org.example.internshipassignmentkafka.dtos.UpdateTaskRequest;
import org.example.internshipassignmentkafka.exception.KafkaPublishFailedException;
import org.example.internshipassignmentkafka.service.SequenceGeneratorService;
import org.example.internshipassignmentkafka.utility.SecurityUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskEventProducer {

    private final KafkaTemplate<String, TaskEvent> kafkaTemplate;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final ObjectMapper objectMapper;

    private static final String TOPIC = "task-events";

    public Mono<Void> publishCreateEvent(CreateTaskRequest request, String username) {
        return sequenceGeneratorService.generateSequence("task_sequence")
                .map(nextId -> String.format("TASK-%03d", nextId))
                .flatMap(taskId ->
                        buildEvent("TASK_CREATED", taskId,
                                new CreateTaskPayload(taskId, request, username))
                );
    }

    public Mono<Void> publishUpdateEvent(String taskId, UpdateTaskRequest request, String username) {
        return buildEvent("TASK_UPDATED", taskId,
                new TaskUpdatedPayload(taskId, request, username));
    }

    public Mono<Void> publishDeleteEvent(String taskId) {
        return buildEvent("TASK_DELETED", taskId, taskId);
    }

    private Mono<Void> buildEvent(String eventType, String key, Object payload) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(payload))
                .map(json -> new TaskEvent(eventType, LocalDateTime.now(), json))
                .flatMap(event -> send(key, event));
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

    public Mono<Void> publishRawEvent(TaskEvent event) {
        return send(event.getEventType(), event);
    }
}