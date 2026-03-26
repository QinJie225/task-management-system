package org.example.internshipassignmentkafka.kafka;

import org.example.internshipassignmentkafka.exception.TaskNotFoundException;
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
    private final WebhookService webhookService;

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

//                yield taskService.existsTaskByTaskId(payload.taskId())
//                        .flatMap(exists -> {
//                            if (exists) {
//                                log.info("Task {} already exists, skipping", payload.taskId());
//                                return Mono.empty();
//                            }
//                            return taskService.createTask(payload.request(), payload.taskId())
//                                    .then();
//                        })
//                        .then(webhookService.sendCallback("TASK_CREATED", payload.taskId()))
//                        .onErrorResume(ex -> webhookService.sendFailureCallback(
//                                "TASK_CREATED", payload.taskId(), ex.getMessage()
//                        ));
                yield taskService.createTask(payload.request(), payload.taskId())
                        .then(
                                webhookService.sendCallback("TASK_CREATED", payload.taskId()))
                        .onErrorResume(RuntimeException.class, ex -> {
                            log.warn(ex.getMessage());
                            return webhookService.sendFailureCallback("TASK_CREATED", payload.taskId(), ex.getMessage());
                        });
            }

            case "TASK_UPDATED" -> {
                TaskUpdatedPayload payload = objectMapper.convertValue(
                        event.getPayload(), TaskUpdatedPayload.class);
                yield taskService.updateTask(payload.taskId(), payload.request())
                        .then(
                                webhookService.sendCallback("TASK_UPDATED", payload.taskId())
                        )
                        .onErrorResume(RuntimeException.class, ex -> {
                            log.warn(ex.getMessage());
                            return webhookService.sendFailureCallback(
                                    "TASK_UPDATED", payload.taskId(), ex.getMessage()
                            );
                        });
            }

            case "TASK_DELETED" -> {
                String taskId = objectMapper.convertValue(event.getPayload(), String.class);
                yield taskService.deleteTask(taskId)
                        .then(webhookService.sendCallback("TASK_DELETED", taskId)
                        )
                        .onErrorResume(RuntimeException.class, ex -> {
                            log.warn(ex.getMessage());
                            return webhookService.sendFailureCallback(
                                    "TASK_DELETED", taskId, ex.getMessage()
                            );
                        });
            }

            default -> {
                log.warn("Unknown event type: {}", event.getEventType());
                yield Mono.empty();
            }
        };
    }
}