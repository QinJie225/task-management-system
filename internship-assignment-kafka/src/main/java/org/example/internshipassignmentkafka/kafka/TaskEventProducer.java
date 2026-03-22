package org.example.internshipassignmentkafka.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.internshipassignmentkafka.dtos.CreateTaskRequest;
import org.example.internshipassignmentkafka.dtos.UpdateTaskRequest;
import org.example.internshipassignmentkafka.exception.KafkaPublishFailedException;
import org.example.internshipassignmentkafka.service.SequenceGeneratorService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskEventProducer {

    private final KafkaTemplate<String, TaskEvent> kafkaTemplate;
    private final SequenceGeneratorService sequenceGeneratorService;

    private static final String TOPIC = "task-events";

    public void publishCreateEvent(CreateTaskRequest request) {
        long nextId = sequenceGeneratorService.generateSequence("task_sequence");
        String taskId = String.format("TASK-%03d", nextId);
        CreateTaskPayload payload = new CreateTaskPayload(taskId, request);
        TaskEvent event = new TaskEvent("TASK_CREATED", LocalDateTime.now(), payload);

        send(taskId, event);
    }

    public void publishUpdateEvent(String taskId, UpdateTaskRequest request) {
        TaskUpdatedPayload payload = new TaskUpdatedPayload(taskId, request);
        TaskEvent event = new TaskEvent("TASK_UPDATED", LocalDateTime.now(), payload);


        send(taskId, event);
    }

    public void publishDeleteEvent(String taskId) {
        TaskEvent event = new TaskEvent("TASK_DELETED", LocalDateTime.now(), taskId);
        send(taskId, event);
    }

    private void send(String key, TaskEvent event) {
        kafkaTemplate.send(TOPIC, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish Kafka Event: {} - {}",
                                event.getEventType(), ex.getMessage(), ex);
                        throw new KafkaPublishFailedException(event.getEventType(), key, ex);
                    } else {
                        log.info("Published Kafka Event: {} — offset: {}",
                                event.getEventType(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}