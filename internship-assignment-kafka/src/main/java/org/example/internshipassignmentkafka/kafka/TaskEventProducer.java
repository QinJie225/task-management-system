package org.example.internshipassignmentkafka.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.internshipassignmentkafka.model.Task;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskEventProducer {

    private final KafkaTemplate<String, TaskEvent> kafkaTemplate;
    private static final String TOPIC = "task-events";

    public void publishEvent(String eventType, Task task) {
        TaskEvent event = new TaskEvent(
                eventType,
                task.getId(),
                task.getTitle(),
                LocalDateTime.now()
        );
        kafkaTemplate.send(TOPIC, event);
        log.info("Published Kafka Event: {} for {}", eventType, task.getId());
    }
}