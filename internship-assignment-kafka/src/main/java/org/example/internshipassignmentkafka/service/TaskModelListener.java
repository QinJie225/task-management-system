package org.example.internshipassignmentkafka.service;

import org.example.internshipassignmentkafka.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
public class TaskModelListener extends AbstractMongoEventListener<Task> {
    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Task> event) {
        Task task = event.getSource();

        if (task.getTaskId() == null || task.getTaskId().isEmpty()) {
            long nextId = sequenceGeneratorService.generateSequence("task_sequence");

            String formattedId = String.format("TASK-%03d", nextId);
            task.setTaskId(formattedId);
        }
    }
}
