package org.example.internshipassignmentkafka.kafka;

import lombok.extern.slf4j.Slf4j;
import org.example.internshipassignmentkafka.exception.KafkaConsumeFailedException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TaskEventConsumer {

    @KafkaListener(topics = "task-events", groupId = "task-group")
    public void consume(TaskEvent event) {
        try {
            log.info("Received Kafka Event: {} for {}", event.getEventType(), event.getTaskId());
        } catch (Exception ex) {
            log.error("Failed to process Kafka Event: {} for {} — {}",
                    event.getEventType(), event.getTaskId(), ex.getMessage(), ex);
            throw new KafkaConsumeFailedException(event.getEventType(), event.getTaskId(), ex);
        }
    }
}