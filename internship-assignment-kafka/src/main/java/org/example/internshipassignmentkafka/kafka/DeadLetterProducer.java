package org.example.internshipassignmentkafka.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeadLetterProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String DEAD_LETTER_TOPIC = "task-events-dead-letter";

    public Mono<Void> send(TaskEvent event, List<String> reasons) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(
                        new DeadLetterEvent(event, reasons)))
                .flatMap(json ->
                        Mono.fromFuture(
                                kafkaTemplate.send(DEAD_LETTER_TOPIC,
                                                event.getEventType(), json)
                                        .toCompletableFuture()
                        ))
                .doOnSuccess(v -> log.warn("Sent to dead letter topic — eventType: {} reasons: {}",
                        event.getEventType(), reasons))
                .doOnError(ex -> log.error("Failed to send to dead letter topic: {}",
                        ex.getMessage(), ex))
                .onErrorResume(ex -> Mono.empty())
                .then();
    }
}
