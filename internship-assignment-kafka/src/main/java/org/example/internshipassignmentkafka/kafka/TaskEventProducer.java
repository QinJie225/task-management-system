package org.example.internshipassignmentkafka.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.internshipassignmentkafka.dtos.CreateTaskRequest;
import org.example.internshipassignmentkafka.dtos.UpdateTaskRequest;
import org.example.internshipassignmentkafka.exception.KafkaPublishFailedException;
import org.example.internshipassignmentkafka.model.Task;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskEventProducer {

    private final KafkaTemplate<String, TaskEvent> kafkaTemplate;
    private static final String TOPIC_CREATED = "task-created-events";
    private static final String TOPIC_UPDATED = "task-updated-events";
    private static final String TOPIC_DELETED = "task-deleted-events";

    public void publishCreateEvent(CreateTaskRequest request) {
        send(TOPIC_CREATED, new TaskEvent("TASK_CREATED", LocalDateTime.now(), request));
    }

    public void publishUpdateEvent(String taskId, UpdateTaskRequest request) {
        send(TOPIC_UPDATED, new TaskEvent("TASK_UPDATED", LocalDateTime.now(), new TaskUpdatedPayload(taskId, request)));
    }

    public void publishDeleteEvent(String taskId) {
        send(TOPIC_DELETED, new TaskEvent("TASK_DELETED", LocalDateTime.now(), taskId));
    }

    public void send(String topic, TaskEvent event) {
        try {
            var result = kafkaTemplate.send(topic, event).join();
            log.info("Published Kafka Event: {} — offset: {}",
                    event.getEventType(),
                    result.getRecordMetadata().offset());
        } catch (Exception ex) {
            log.error("Failed to publish Kafka Event: {} — {}",
                    event.getEventType(), ex.getMessage());
            throw new KafkaPublishFailedException(event.getEventType(), null, ex);
        }
    }
}