package org.example.internshipassignmentkafka.kafka;

import reactor.core.publisher.Mono;
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

        buildReactiveChain(event)
                .doOnError(ex -> log.error("Failed to process {} Event: {}",
                        event.getEventType(), ex.getMessage(), ex))
                .onErrorMap(ex -> new KafkaConsumeFailedException(event.getEventType(), null, ex))
                .block();
    }

    private Mono<Void> buildReactiveChain(TaskEvent event) {
        return switch (event.getEventType()) {

            case "TASK_CREATED" -> {
                CreateTaskPayload payload = objectMapper.convertValue(
                        event.getPayload(), CreateTaskPayload.class);

                yield taskService.exists(payload.taskId())
                        .flatMap(exists -> {
                            if (exists) {
                                log.info("Task {} already exists, skipping", payload.taskId());
                                return Mono.empty();
                            }
                            return taskService.createTask(payload.request(), payload.taskId())
                                    .then();
                        });
            }

            case "TASK_UPDATED" -> {
                TaskUpdatedPayload payload = objectMapper.convertValue(
                        event.getPayload(), TaskUpdatedPayload.class);
                yield taskService.updateTask(payload.taskId(), payload.request())
                        .doOnError(ex -> log.error("Unexpected error updating task {}: {}",
                                payload.taskId(), ex.getMessage()))
                        .then();
            }

            case "TASK_DELETED" -> {
                String taskId = objectMapper.convertValue(event.getPayload(), String.class);
                yield taskService.deleteTask(taskId)
                        .doOnError(ex -> log.error("Unexpected error deleting task {}: {}",
                        taskId, ex.getMessage()));
            }

            default -> {
                log.warn("Unknown event type: {}", event.getEventType());
                yield Mono.empty();
            }
        };
    }
}