package org.example.internshipassignmentkafka.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import reactor.core.publisher.Mono;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.internshipassignmentkafka.exception.KafkaConsumeFailedException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskEventConsumer {
    private final TaskEventHandlerRegistry registry;
    private final TaskEventValidator validator;
    private final DeadLetterProducer deadLetterProducer;

    @KafkaListener(topics = "task-events", groupId = "task-group")
    public void consume(TaskEvent event, Consumer<String, TaskEvent> consumer) {
        log.info("Received Kafka Event: {}", event.getEventType());

        List<String> errors = validator.validate(event);
        if (!errors.isEmpty()) {
            log.warn("Invalid event received, routing to dead letter — reasons: {}", errors);
            deadLetterProducer.send(event, errors)
                    .doOnSuccess(v -> consumer.commitAsync((offsets, exception) -> {
                        if (exception != null) {
                            log.error("Commit failed after dead letter routing: {}", exception.getMessage());
                        } else {
                            log.info("Offset committed after dead letter routing");
                        }
                    }))
                    .block();
            return;
        }

        registry.findEventType(event.getEventType())
                .map(handler -> handler.handle(event))
                .orElseGet(() -> {
                    log.warn("No handler registered for event type: {}", event.getEventType());
                    return Mono.empty();
                })
                .doOnSuccess(v ->
                        consumer.commitAsync((offsets, exception) -> {
                            if (exception != null) {
                                log.error("Commit failed for event: {} — offsets: {} — reason: {}",
                                        event.getEventType(), offsets, exception.getMessage());
                            } else {
                                log.info("Commit succeeded for event: {} — offsets: {}",
                                        event.getEventType(), offsets);
                            }
                        })
                )
                .doOnError(ex -> log.error("Failed to process {} event: {}",
                        event.getEventType(), ex.getMessage(), ex))
                .onErrorMap(ex -> new KafkaConsumeFailedException(
                        event.getEventType(), null, ex))
                .onErrorResume(KafkaConsumeFailedException.class, ex ->
                        deadLetterProducer.send(event, List.of(ex.getMessage()))
                                .doOnSuccess(v -> consumer.commitAsync((offsets, exception) -> {
                                    if (exception != null) {
                                        log.error("Commit failed after dead letter routing: {}", exception.getMessage());
                                    } else {
                                        log.info("Offset committed after dead letter routing");
                                    }
                                }))
                )
                .block();
    }
}