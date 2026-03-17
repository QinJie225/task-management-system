package org.example.internshipassignmentkafka.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.internshipassignmentkafka.exception.KafkaPublishFailedException;
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
                task.getTaskId(),
                task.getTitle(),
                LocalDateTime.now()
        );
        try {
            var result = kafkaTemplate.send(TOPIC, event).join();
            log.info("Published Kafka Event: {} for {} — offset: {}",
                    eventType, task.getTaskId(),
                    result.getRecordMetadata().offset());
        } catch (Exception ex) {
            log.error("Failed to publish Kafka Event: {} for {} — {}",
                    eventType, task.getTaskId(), ex.getMessage());
            throw new KafkaPublishFailedException(eventType, task.getTaskId(), ex);
        }
    }
}