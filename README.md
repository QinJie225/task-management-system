# Java Intern Assignment - Task Management System with Kafka

A reactive RESTful Task Management System built with Spring Boot WebFlux, Apache Kafka, and MongoDB. It demonstrates full CRUD operations with an event-driven, asynchronous architecture - the API publishes Kafka events, which a consumer processes to persist data and notify downstream systems via webhooks.

## Table of Contents
- [Architecture Overview](#architecture-overview)
- [Installation](#installation)
- [Usage Example](#usage-example)
- [API Endpoints](#api-endpoints)
- [Features](#features)
- [Kafka Integration](#kafka-integration)
- [Webhook Integration](#webhook-integration)
- [Exception Handling](#exception-handling)
- [Business Rules & Assumptions](#business-rules--assumptions)
- [Project Structure](#project-structure)
- [Tech Stack](#tech-stack)

## Architecture Overview
This application applies Event-Driven Architecture and CQRS pattern. Controllers immediately publish a Kafka event. The Kafka consumer then handles the actual database operation and fires a webhook callback on success or failure.
   ```
   Controller (Send API POST/PATCH/DELETE) → TaskEventProducer → Kafka Broker (task_events topic) → TaskEventConsumer → Service(Saving to MongoDB) 
                                                                                                          ↑
                                                                                                      WebClient
                                                                     
   ```

## Installation
### Prerequisites
- Java 21
- Maven 3.9+
- MongoDB (default: localhost:27017)
- Apache Kafka (default: localhost:9092) with Zookeeper

### Steps
1. Clone the repository
   ```bash
   git clone https://github.com/your-username/internship-assignment-kafka.git
   cd internship-assignment-kafka
   ```

2. Open the project in IntelliJ IDEA

3. Ensure Maven dependencies are installed automatically

4. Start a MongoDB instance (default connection is expected on `localhost:27017`)

5. Configure database and Kafka by editing `src/main/resources/application-docker.properties` 

   ```properties
   spring.application.name=internship-assignment-kafka
   
   # MongoDB
   spring.mongodb.uri=mongodb://mongo:27017/TaskManagementSystem
   
   # Kafka Broker
   spring.kafka.bootstrap-servers=kafka:9092
   
   # Producer config
   spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
   spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
   spring.kafka.producer.properties.spring.json.add.type.headers=false
   
   # Consumer config
   spring.kafka.consumer.group-id=task-group
   spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
   spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
   spring.kafka.consumer.properties.spring.json.trusted.packages=*
   spring.kafka.consumer.properties.spring.json.value.default.type=org.example.internshipassignmentkafka.kafka.TaskEvent
   spring.kafka.consumer.properties.spring.json.use.type.headers=false
   
   logging.level.org.apache.kafka.clients.consumer.internals.ConsumerCoordinator=WARN
   logging.level.org.apache.kafka.clients.consumer.internals.ClassicKafkaConsumer=WARN
   logging.level.org.apache.kafka.clients.NetworkClient=WARN
   logging.level.org.apache.kafka.clients.Metadata=WARN
   ```
6. Configure Webhook Tester by editing `src/main/resources/application.properties`
   ```properties
   webhook.callback-url=https://webhooktest.net/webhook
   webhook.callback-path=/{bucketID}
   ```

## Usage Example
1. Running with Docker

   The project includes a docker-compose.yml that starts MongoDB, Zookeeper, Kafka, and the application together
   ```bash
   docker compose up --build
   ```
   To stop and remove all containers:
   ```bash
   docker compose down
   ```
2. Test the API endpoints using Postman
3. Monitor Kafka events in real time
   ```bash
   docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic task-events --from-beginning
   ````
4. Monitor Spring Boot application logging in real time
   ```bash
   docker compose logs -f app
   ````
5. Watch WebHook Tester web page (Reference: https://webhooktest.net/bucket/019d27dc-df0f-7124-afd4-befd21eb209c)

## API Endpoints
### Tasks
All write operations return `202 Accepted` immediately 

| Method   | Endpoint              | Description                              | Response          |
|----------|-----------------------|------------------------------------------|-------------------|
| `POST`   | `/api/tasks`          | Publish a create event for a new task    | `202 Accepted`    |
| `GET`    | `/api/tasks`          | Get all tasks (optionally filter by status) | `200 OK`       |
| `GET`    | `/api/tasks/{taskId}` | Get a specific task by ID                | `200 OK`          |
| `PATCH`  | `/api/tasks/{taskId}` | Publish an update event for a task       | `202 Accepted`    |
| `DELETE` | `/api/tasks/{taskId}` | Publish a delete event for a task        | `202 Accepted`    |
#### Query Parameters
- **status** (optional): Filter tasks by status (`PENDING`, `IN_PROGRESS`, `COMPLETED`)

## Features

### Task Management
- Full CRUD with a reactive, non-blocking stack (Spring WebFlux + Reactive MongoDB)
- Each task has a unique `taskId` (application-generated) and a MongoDB `_id`
- Tasks include title, description, priority, due date, and status
- Automatic `createdAt` and `updatedAt` timestamp tracking

### Task Properties
| Field         | Rules                                   |
|---------------|-----------------------------------------|
| `taskId`      | Unique application-generated identifier |
| `title`       | Required, 3–35 characters               |
| `description` | Required, 3–100 characters              |
| `priority`    | Required - `LOW`, `MEDIUM`, `HIGH`      |
| `status`      | `PENDING` on creation; updatable        |
| `dueDate`     | Required, present or future date        |
| `createdAt`   | Set on creation                         |
| `updatedAt`   | Updated on every modification           |

### Validation
- Enforced via Jakarta Bean Validation on all request DTOs
- `@NotBlank`, `@Size`, `@NotNull`, `@FutureOrPresent` on create requests
- `@Pattern` + `@Size` on optional update fields (allows null but rejects blank strings)
- All validation messages are centralized in `ValidationMessages`

## Kafka Integration

### How It Works

1. The controller calls a `TaskEventProducer` method, which wraps the request in a `TaskEvent` and sends it to the `task-events` topic.
2. The `TaskEventConsumer` picks up the event and routes it to the appropriate `TaskService` method.
3. After the database operation, a webhook callback is fired.

### Topic

| Topic          | Partitions | Replicas |
|----------------|-----------|---------|
| `task-events`  | 3         | 1       |

### TaskEvent Structure

```java
public class TaskEvent {
   private String eventType;    // "TASK_CREATED", "TASK_UPDATED", or "TASK_DELETED"
   private LocalDateTime timestamp;
   private Object payload;      // CreateTaskPayload | TaskUpdatedPayload | String (taskId)
}
```

### Event Payloads

| Event Type     | Payload Type          | Contents                            |
|----------------|-----------------------|-------------------------------------|
| `TASK_CREATED` | `CreateTaskPayload`   | `taskId` + `CreateTaskRequest`      |
| `TASK_UPDATED` | `TaskUpdatedPayload`  | `taskId` + `UpdateTaskRequest`      |
| `TASK_DELETED` | `String`              | `taskId`                            |

## Webhook Integration

After each Kafka event is processed, the system sends an HTTP POST callback to a configured webhook URL.

### Configuration
   ```properties
    webhook.callback-url=https://webhooktest.net/webhook
    webhook.callback-path=/{bucketID}
   ```
### Callback Payload
   ```json
   {
     "eventType": "TASK_CREATED",
     "taskId": "abc-123",
     "status": "SUCCESS",
     "message": "Task processed successfully",
     "processedAt": "2025-07-01T10:30:00"
   }
   ```
| Field         | Values              | Description                          |
|---------------|---------------------|--------------------------------------|
| `status`      | `SUCCESS`, `FAILED` | Outcome of the database operation    |
| `message`     | String              | Success message or error description |
| `processedAt` | LocalDateTime       | When the event was processed         |

Webhook failures are logged but do not interrupt event processing - the consumer continues running.

## Exception Handling

All exceptions are handled by `GlobalExceptionHandler` and return a consistent `ApiErrorResponse` body.

| Exception                      | HTTP Status | Trigger                                                                 |
|--------------------------------|-------------|-------------------------------------------------------------------------|
| `TaskNotFoundException`        | `404`       | `taskId` does not exist in MongoDB                                      |
| `EmptyUpdateRequestException`  | `400`       | PATCH request body has no fields set                                    |
| `KafkaPublishFailedException`  | `500`       | Kafka event could not be published from the controller                  |
| `WebExchangeBindException`     | `400`       | Bean validation fails on a request field (WebFlux equivalent of `MethodArgumentNotValidException`) |
| `ServerWebInputException`      | `400`       | Malformed request body or invalid enum value in JSON (WebFlux equivalent of `HttpMessageNotReadableException`) |

## Business Rules & Assumptions

1. **Async writes** - All create, update, and delete operations go through Kafka. The controller never writes to MongoDB directly.
2. **Partial updates** - Only fields present in the `UpdateTaskRequest` are applied; unset fields remain unchanged.
3. **Status defaults** - Tasks are always created with `PENDING` status regardless of any client input.
4. **Webhook effort** - Failures in webhook delivery are logged and swallowed; they do not roll back the database operation.
5. **Event publishing failures** - If publishing to Kafka fails, the HTTP response will propagate the error. The database is not touched until the consumer picks up the event.

## Project Structure

```
src/main/java/org/example/internshipassignmentkafka/
├── config/
│   └── WebClientConfig.java                       # WebClient bean configured with webhook base URL
├── controller/
│   └── TaskController.java                        # Reactive REST endpoints (WebFlux)
├── dtos/
│   ├── CreateTaskRequest.java                     # Create request DTO (validated record)
│   ├── TaskResponse.java                          # Task response DTO
│   └── UpdateTaskRequest.java                     # Partial update request DTO
├── enums/
│   ├── TaskPriority.java                          # LOW, MEDIUM, HIGH
│   └── TaskStatus.java                            # PENDING, IN_PROGRESS, COMPLETED
├── exception/
│   ├── ApiErrorResponse.java                      # Standardized error response body
│   ├── FieldErrorDto.java                         # Per-field validation error detail
│   ├── GlobalExceptionHandler.java                # Centralized exception -> HTTP mapping
│   ├── EmptyUpdateRequestException.java           # Thrown when PATCH body has no fields
│   ├── KafkaConsumeFailedException.java           # Wraps unexpected consumer errors
│   ├── KafkaPublishFailedException.java           # Wraps unexpected producer errors
│   └── TaskNotFoundException.java                 # Thrown when taskId is not found
├── kafka/
│   ├── KafkaConfig.java                           # Topic and error handler beans
│   ├── TaskEvent.java                             # Kafka message envelope
│   ├── TaskEventConsumer.java                     # Listener - routes events to service + webhook
│   ├── TaskEventProducer.java                     # Publishes events to task-events topic
│   ├── CreateTaskPayload.java                     # Payload for TASK_CREATED events
│   ├── TaskUpdatedPayload.java                    # Payload for TASK_UPDATED events
│   ├── WebhookPayload.java                        # Outbound webhook callback body
│   └── WebhookService.java                        # Sends success/failure HTTP callbacks
├── mapper/
│   └── TaskMapper.java                            # MapStruct: entity <-> DTO, partial update
├── model/
│   ├── DatabaseSequence.java                      # MongoDB sequence generator
│   └── Task.java                                  # MongoDB document
├── repository/
│   └── TaskRepository.java                        # Reactive MongoDB repository
├── service/
│   ├── TaskService.java                           # Service interface
│   └── impl/
│       └── TaskServiceImpl.java                   # Business logic (reactive)
└── utility/
    └── ValidationMessages.java                    # Validation message constants
 
InternshipAssignmentKafkaApplication.java          # Spring Boot entry point
```

## Tech Stack
### Core Technologies
- Java 17
- Spring Boot 4.0.3
- Spring Webflux
- Spring Data MongoDB
- Zookeeper (Image: confluentinc/cp-zookeeper:7.6.0)
- Kafka (Image: confluentinc/cp-kafka:7.6.0)
- MongoDB (Image: mongo:7.0)

### Libraries & Tools
- Lombok
- MapStruct 1.5.5
- Jakarta Bean Validation
- Jackson Databind
- Maven

### Development Tools
- IntelliJ IDEA
- Postman
- Docker/Docker Compose
- MongoDB Compass
- Rancher Desktop
- Webhook Tester
