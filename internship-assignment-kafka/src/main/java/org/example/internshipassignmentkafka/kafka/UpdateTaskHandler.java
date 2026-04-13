package org.example.internshipassignmentkafka.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.internshipassignmentkafka.service.TaskService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateTaskHandler implements TaskEventHandler {
    private final TaskService taskService;
    private final WebhookService webhookService;
    private final ObjectMapper objectMapper;

    @Override
    public String getEventType() {
        return "TASK_UPDATED";
    }

    @Override
    public Mono<Void> handle(TaskEvent event) {
        return Mono.fromCallable(() ->
                        objectMapper.readValue(event.getPayload(), TaskUpdatedPayload.class))
                .flatMap(payload ->
                        taskService.updateTask(payload.taskId(), payload.request())
                                .then(webhookService.sendCallback("TASK_UPDATED", payload.taskId()))
                                .onErrorResume(RuntimeException.class, ex -> {
                                    log.warn(ex.getMessage());
                                    return webhookService.sendFailureCallback(
                                            "TASK_UPDATED", payload.taskId(), ex.getMessage());
                                }));
    }
}