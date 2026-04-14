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
public class CreateTaskHandler implements TaskEventHandler {
    private final TaskService taskService;
    private final WebhookService webhookService;
    private final ObjectMapper objectMapper;

    @Override
    public String getEventType() {
        return "TASK_CREATED";
    }

    @Override
    public Mono<Void> handle(TaskEvent event) {
        return Mono.fromCallable(() ->
                        objectMapper.readValue(event.getPayload(), CreateTaskPayload.class))
                .flatMap(payload ->
                        taskService.createTask(payload.request(), payload.taskId(), payload.actorUsername())
                                .then(webhookService.sendCallback("TASK_CREATED", payload.taskId()))
                                .onErrorResume(RuntimeException.class, ex -> {
                                    log.warn(ex.getMessage());
                                    return webhookService.sendFailureCallback(
                                            "TASK_CREATED", payload.taskId(), ex.getMessage());
                                }));
    }
}
