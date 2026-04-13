package org.example.internshipassignmentkafka.kafka;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class TaskEventHandlerRegistry {
    private final Map<String, TaskEventHandler> handlers;

    public TaskEventHandlerRegistry(List<TaskEventHandler> handlerList) {
        this.handlers = handlerList.stream()
                .collect(Collectors.toUnmodifiableMap(
                        TaskEventHandler::getEventType,
                        Function.identity()
                ));
    }

    public Optional<TaskEventHandler> findEventType(String eventType) {
        return Optional.ofNullable(handlers.get(eventType));
    }
}
