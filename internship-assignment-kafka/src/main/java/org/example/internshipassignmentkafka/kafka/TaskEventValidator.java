package org.example.internshipassignmentkafka.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskEventValidator {

    private final ObjectMapper objectMapper;
    private final Map<String, JsonSchema> schemaCache;

    private static final JsonSchemaFactory factory =
            JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);

    private static final Set<String> VALID_EVENT_TYPES = Set.of(
            "TASK_CREATED", "TASK_UPDATED", "TASK_DELETED"
    );

    private static final Map<String, String> SCHEMA_PATHS = Map.of(
            "TASK_CREATED", "schemas/task-created.json",
            "TASK_UPDATED", "schemas/task-updated.json",
            "TASK_DELETED", "schemas/task-deleted.json"
    );

    @Autowired
    public TaskEventValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.schemaCache = loadSchemas();
    }

    private Map<String, JsonSchema> loadSchemas() {
        Map<String, JsonSchema> cache = new HashMap<>();
        SCHEMA_PATHS.forEach((eventType, path) -> {
            try (InputStream is = new ClassPathResource(path).getInputStream()) {
                cache.put(eventType, factory.getSchema(is));
                log.info("Loaded schema for: {}", eventType);
            } catch (IOException ex) {
                log.error("Failed to load schema for {}: {}", eventType, ex.getMessage());
            }
        });
        return cache;
    }

    public List<String> validate(TaskEvent event) {
        List<String> errors = new ArrayList<>();

        errors.addAll(validateEvent(event));
        if (!errors.isEmpty()) return errors;

        errors.addAll(validateSchema(event));
        if (!errors.isEmpty()) return errors;

        errors.addAll(validateDueDate(event));

        return errors;
    }

    public boolean isValid(TaskEvent event) {
        return validate(event).isEmpty();
    }

    private List<String> validateEvent(TaskEvent event) {
        List<String> errors = new ArrayList<>();

        if (event == null) {
            errors.add("Event is null");
            return errors;
        }
        if (event.getEventType() == null || event.getEventType().isBlank()) {
            errors.add("Event type is null or blank");
        } else if (!VALID_EVENT_TYPES.contains(event.getEventType())) {
            errors.add("Unknown event type: " + event.getEventType());
        }
        if (event.getPayload() == null || event.getPayload().isBlank()) {
            errors.add("Event payload is null or blank");
        }

        return errors;
    }

    private List<String> validateSchema(TaskEvent event) {
        List<String> errors = new ArrayList<>();

        try {
            JsonSchema schema = schemaCache.get(event.getEventType());
            if (schema == null) {
                errors.add("No schema found for event type: " + event.getEventType());
                return errors;
            }

            JsonNode payloadNode;

            if (event.getEventType().equals("TASK_DELETED")) {
                String taskId = event.getPayload().replace("\"", "").trim();
                payloadNode = objectMapper.getNodeFactory().textNode(taskId);
            } else {
                payloadNode = objectMapper.readTree(event.getPayload());
            }

            Set<ValidationMessage> violations = schema.validate(payloadNode);
            violations.forEach(v ->
                    errors.add("Schema violation: " + v.getMessage()));

        } catch (Exception ex) {
            errors.add("Failed to validate schema: " + ex.getMessage());
        }

        return errors;
    }

    private List<String> validateDueDate(TaskEvent event) {
        List<String> errors = new ArrayList<>();

        try {
            JsonNode node = objectMapper.readTree(event.getPayload());

            if (event.getEventType().equals("TASK_CREATED") ||
                    event.getEventType().equals("TASK_UPDATED")) {

                JsonNode dueDate = node.path("request").path("dueDate");
                if (!dueDate.isMissingNode() && !dueDate.isNull()) {
                    errors.addAll(checkDueDateNotPast(
                            dueDate.asText(), event.getEventType()));
                }
            }
        } catch (Exception ex) {
            errors.add("Failed to validate dueDate: " + ex.getMessage());
        }

        return errors;
    }

    private List<String> checkDueDateNotPast(String dueDateStr, String eventType) {
        List<String> errors = new ArrayList<>();
        try {
            LocalDate dueDate = LocalDate.parse(dueDateStr);
            if (dueDate.isBefore(LocalDate.now())) {
                errors.add("[" + eventType + "] dueDate must be present or future, got: " + dueDateStr);
            }
        } catch (Exception ex) {
            errors.add("[" + eventType + "] dueDate invalid format, expected yyyy-MM-dd, got: " + dueDateStr);
        }
        return errors;
    }
}