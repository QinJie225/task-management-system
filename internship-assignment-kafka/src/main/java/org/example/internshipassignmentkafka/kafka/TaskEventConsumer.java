package org.example.internshipassignmentkafka.kafka;

import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.internshipassignmentkafka.exception.KafkaConsumeFailedException;
import org.example.internshipassignmentkafka.service.TaskService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskEventConsumer {

    private final TaskService taskService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "task-events", groupId = "task-group")
    public void consume(TaskEvent event) {
        log.info("Received Kafka Event: {}", event.getEventType());

        try {
            switch (event.getEventType()) {

                case "TASK_CREATED" -> {
                    CreateTaskPayload payload = objectMapper.convertValue(
                            event.getPayload(), CreateTaskPayload.class
                    );

                    if (taskService.exists(payload.taskId())) {
                        log.info("Task {} already exists, skipping duplicate", payload.taskId());
                        return;
                    }
                    taskService.createTask(payload.request(), payload.taskId());
                }

                case "TASK_UPDATED" -> {
                    TaskUpdatedPayload payload = objectMapper.convertValue(
                            event.getPayload(), TaskUpdatedPayload.class
                    );
                    taskService.updateTask(payload.taskId(), payload.request());
                }

                case "TASK_DELETED" -> {
                    String taskId = objectMapper.convertValue(event.getPayload(), String.class);
                    taskService.deleteTask(taskId);
                }

                default -> log.warn("Unknown event type: {}", event.getEventType());
            }

        } catch (Exception ex) {
            log.error("Failed to process {} Event: {}",
                    event.getEventType(), ex.getMessage(), ex);
            throw new KafkaConsumeFailedException(event.getEventType(), null, ex);
        }
    }
}