package org.example.internshipassignmentkafka.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.internshipassignmentkafka.exception.KafkaConsumeFailedException;
import org.example.internshipassignmentkafka.service.TaskService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskEventConsumer {
    private final TaskService taskService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "task-created-events", groupId = "task-group")
    public void consumeCreated(TaskEvent event) {
        log.info("Received Kafka Event: {}", event.getEventType());

        try {
            CreateTaskPayload payload = objectMapper.convertValue(event.getPayload(), CreateTaskPayload.class);

            if (taskService.exists(payload.taskId())) {
                log.info("Task {} already exists, skipping duplicate", payload.taskId());
                return;
            }
            taskService.createTask(payload.request(), payload.taskId());
        } catch (Exception ex) {
            log.error("Failed to process TASK_CREATED Event: {} — {}", event.getEventType(), ex.getMessage(), ex);
            throw new KafkaConsumeFailedException(event.getEventType(), null, ex);
        }
    }

    @KafkaListener(topics = "task-updated-events", groupId = "task-group")
    public void consumeUpdated(TaskEvent event) {
        log.info("Received Kafka Event: {}", event.getEventType());

        try {
            TaskUpdatedPayload payload = objectMapper.convertValue(event.getPayload(), TaskUpdatedPayload.class);
            taskService.updateTask(payload.taskId(), payload.request());
        } catch (Exception ex) {
            log.error("Failed to process TASK_UPDATED Event: {} — {}", event.getEventType(), ex.getMessage(), ex);
            throw new KafkaConsumeFailedException(event.getEventType(), null, ex);
        }
    }

    @KafkaListener(topics = "task-deleted-events", groupId = "task-group")
    public void consumeDeleted(TaskEvent event) {
        log.info("Received Kafka Event: {}", event.getEventType());

        try {
            String taskId = objectMapper.convertValue(event.getPayload(), String.class);
            taskService.deleteTask(taskId);
        } catch (Exception ex) {
            log.error("Failed to process TASK_DELETED Event: {} — {}", event.getEventType(), ex.getMessage(), ex);
            throw new KafkaConsumeFailedException(event.getEventType(), null, ex);
        }
    }
}