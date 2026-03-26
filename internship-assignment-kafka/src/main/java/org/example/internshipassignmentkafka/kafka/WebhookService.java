package org.example.internshipassignmentkafka.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {
    private final WebClient webClient;
    @Value("${webhook.callback-path}")
    private String callbackPath;

    public Mono<Void> sendCallback(String eventType, String taskId) {
        return send(new WebhookPayload(eventType, taskId, "SUCCESS", "Task processed successfully", LocalDateTime.now()));
    }

    public Mono<Void> sendFailureCallback(String eventType, String taskId, String errorMessage) {
        return send(new WebhookPayload(eventType, taskId, "FAILED", errorMessage, LocalDateTime.now()));
    }

    private Mono<Void> send(WebhookPayload payload) {
        return webClient.post()
                .uri(callbackPath)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("Webhook notified: eventType={}, taskId={}, status={}",
                        payload.eventType(), payload.taskId(), payload.status()))
                .doOnError(ex -> log.error("Webhook failed: eventType={}, taskId={}, error={}",
                        payload.eventType(), payload.taskId(), ex.getMessage()))
                .onErrorResume(ex -> Mono.empty());
    }
}
