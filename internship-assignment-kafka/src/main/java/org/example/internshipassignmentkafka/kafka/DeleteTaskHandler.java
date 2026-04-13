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
public class DeleteTaskHandler implements TaskEventHandler {
    private final TaskService taskService;
    private final WebhookService webhookService;
    private final ObjectMapper objectMapper;

    @Override
    public String getEventType() {
        return "TASK_DELETED";
    }

    @Override
    public Mono<Void> handle(TaskEvent event) {
        return Mono.fromCallable(() ->
                        objectMapper.readValue(event.getPayload(), String.class))
                .flatMap(taskId ->
                        taskService.deleteTask(taskId)
                                .then(webhookService.sendCallback("TASK_DELETED", taskId))
                                .onErrorResume(RuntimeException.class, ex -> {
                                    log.warn(ex.getMessage());
                                    return webhookService.sendFailureCallback(
                                            "TASK_DELETED", taskId, ex.getMessage());
                                }));
    }
}