package org.example.internshipassignmentkafka.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskEvent {

    private String eventType;
    private String taskId;
    private String title;
    private LocalDateTime timestamp;
}