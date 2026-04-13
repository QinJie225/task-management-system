package org.example.internshipassignmentkafka.kafka;

import reactor.core.publisher.Mono;

public interface TaskEventHandler {
    String getEventType();
    Mono<Void> handle(TaskEvent event);
}
