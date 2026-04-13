package org.example.internshipassignmentkafka.kafka;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeadLetterEvent {
    private TaskEvent originalEvent;
    private List<String> reasons;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime failedAt;

    public DeadLetterEvent(TaskEvent originalEvent, List<String> reasons) {
        this.originalEvent = originalEvent;
        this.reasons = reasons;
        this.failedAt = LocalDateTime.now();
    }
}