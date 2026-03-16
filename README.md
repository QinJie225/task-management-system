# Java Intern Assignment - Task Management System with Kafka

A RESTful Task Management System built with Spring Boot, Apache Kafka, and MongoDB, demonstrating full CRUD operations with event-driven messaging via Kafka and data persistence via MongoDB.
## Table of Contents

- [Installation](#installation)
- [Usage Example](#usage-example)
- [API Endpoints](#api-endpoints)
- [Features](#features)
- [Kafka Integration](#kafka-integration)
- [Exception Handling](#exception-handling)
- [Business Rules & Assumptions](#business-rules--assumptions)
- [Project Structure](#project-structure)
- [Tech Stack](#tech-stack)

## Installation

1. Clone the repository

   ```bash
   git clone https://github.com/your-username/internship-assignment-kafka.git
   ```

2. Open the project in IntelliJ IDEA

3. Ensure Maven dependencies are installed automatically

4. Start a MongoDB instance (default connection is expected on `localhost:27017`)

5. Start Zookeeper in command prompt 

6. Start Apache Kafka (default connection is expected on `localhost:9092`) in command prompt

7. Configure database and Kafka by editing `src/main/resources/application.properties` 

   ```properties
   spring.application.name=internship-assignment-kafka
   
   spring.mongodb.host=localhost
   spring.mongodb.port=27017
   spring.mongodb.database=<database>
   
   # Kafka Broker
   spring.kafka.bootstrap-servers=localhost:9092
   
   # Producer config
   spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
   spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
   
   # Consumer config
   spring.kafka.consumer.group-id=task-group
   spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
   spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
   spring.kafka.consumer.properties.spring.json.trusted.packages=*
   spring.kafka.consumer.properties.spring.json.value.default.type=org.example.internshipassignmentkafka.kafka.TaskEvent
   spring.kafka.producer.properties.spring.json.add.type.headers=false
   ```
7. Run the application

   ```bash
   mvn spring-boot:run
   ```

## Usage Example
1. Run `src/main/java/org/example/internshipassignmentkafka/InternshipAssignmentKafkaApplication.java` to start the application
2. Test the API endpoints using Postman
3. Monitor Kafka events through application logs


## API Endpoints
### Tasks
| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/tasks` | Create a new task |
| `GET` | `/api/tasks` | Get all tasks (optionally filtered by status) |
| `GET` | `/api/tasks/{taskId}` | Get a specific task by ID |
| `PATCH` | `/api/tasks/{taskId}` | Update a task |
| `DELETE` | `/api/tasks/{taskId}` | Delete a task |

#### Query Parameters
- **status** (optional): Filter tasks by status (`PENDING`, `IN_PROGRESS`, `COMPLETED`)

## Features

### Task Management
- Create, read, update, and delete tasks
- Each task has a unique `taskId` and MongoDB `id`
- Tasks include title, description, priority, due date, and status
- Automatic timestamp tracking for creation and last modification

### Task Properties
- **Title**: 3-35 characters, required
- **Description**: 3-100 characters, required  
- **Priority**: LOW, MEDIUM, or HIGH
- **Status**: PENDING, IN_PROGRESS, or COMPLETED
- **Due Date**: Required date field
- **Auto-generated fields**: taskId, createdAt, updatedAt

### Validation
- All request fields are validated using Jakarta Bean Validation
- Invalid requests return `400 Bad Request` with field-level error details
- Custom validation messages centralized in `ValidationMessages` utility class

## Kafka Integration

### Event Publishing
The system publishes task events to Kafka when tasks are created or updated:

#### TaskEvent Structure
```java
public class TaskEvent {
    private String eventType;    // "TASK_CREATED" or "TASK_UPDATED"
    private String taskId;       // Unique task identifier
    private String title;        // Task title
    private LocalDateTime timestamp; // Event timestamp
}
```

#### Kafka Configuration
- **Topic**: Task events are published to the default topic
- **Producer**: JSON serialization with String keys
- **Consumer**: Configured for task-group with JSON deserialization
- **Trusted Packages**: All packages are trusted for JSON deserialization

### Event Types
- **TASK_CREATED**: Published when a new task is created
- **TASK_UPDATED**: Published when an existing task is updated
- **TASK_DELETED**: Published when an existing task is deleted

## Exception Handling
| Exception                           | HTTP Status | Trigger                                                   |
|-------------------------------------|---|-----------------------------------------------------------|
| EmptyUpdateRequestException         | `400 Bad Request` | Empty update request in PATCH request                     |
| TaskNotFoundException               | `404 Not Found` | Task ID does not exist                                    |
| MethodArgumentNotValidException     | `400 Bad Request` | Invalid request body fields failing validation constraints |
| MethodArgumentTypeMismatchException | `400 Bad Request` | Invalid query or path parameter values                    |
| General Exceptions                  | `500 Internal Server Error`|  Unexpected server-side errors |

## Business Rules & Assumptions

### 1. Task Creation
- Tasks must have a title (3-35 characters)
- Tasks must have a description (3-100 characters)
- Tasks must have a priority (LOW, MEDIUM, HIGH)
- Tasks must have a due date
- Tasks are created with PENDING status by default
- A unique taskId is automatically generated for each task

### 2. Task Updates
- Only the fields provided in the request are updated
- Task status can be changed to any valid status
- Timestamps are automatically updated when tasks are modified

### 3. Task Deletion
- Tasks can be deleted by their taskID
- Deletion is permanent and cannot be undone

### 4. Kafka Events
- Events are published asynchronously after database operations
- Events contain essential task information for downstream consumers
- Event publishing failures do not affect the main task operations

## Project Structure
```
src/main/java/org/example/internshipassignmentkafka/
├── controller/
│   └── TaskController.java                        # REST API endpoints for task management
├── dtos/
│   ├── CreateTaskRequest.java                     # Request DTO for creating tasks
│   ├── TaskResponse.java                          # Response DTO for task data
│   └── UpdateTaskRequest.java                     # Request DTO for updating tasks
├── enums/
│   ├── TaskPriority.java                          # LOW, MEDIUM, HIGH priority levels
│   └── TaskStatus.java                             # PENDING, IN_PROGRESS, COMPLETED
├── exception/
│   ├── ApiErrorResponse.java                      # Standardized error response body
│   ├── FieldErrorDto.java                         # DTO for field-level validation errors
│   ├── GlobalExceptionHandler.java                # Centralized exception handling
│   ├── EmptyUpdateRequestException.java           # Thrown when update request contains no fields
│   └── TaskNotFoundException.java                 # Thrown when a task cannot be found
├── kafka/
│   ├── KafkaConfig.java                           # Kafka producer/consumer configuration
│   ├── TaskEvent.java                             # Event model for Kafka messages
│   ├── TaskEventConsumer.java                     # Kafka event consumer
│   └── TaskEventProducer.java                     # Kafka event producer
├── mapper/
│   └── TaskMapper.java                           # Maps Task entity ↔ DTO
├── model/
│   ├── DatabaseSequence.java                      # MongoDB sequence generator
│   └── Task.java                                   # MongoDB document for tasks
├── repository/
│   └── TaskRepository.java                        # MongoDB repository for tasks
├── service/
│   ├── TaskService.java                           # Task service interface
│   └── impl/
│       └── TaskServiceImpl.java                   # Task business logic implementation
└── utility/
    └── ValidationMessages.java                    # Centralized validation message constants

InternshipAssignmentKafkaApplication.java           # Spring Boot application entry point
```

## Tech Stack
### Core Technologies
- Java 17
- Spring Boot 4.0.3
- Spring Data MongoDB
- Apache Kafka 2.12-3.5.2
- MongoDB

### Libraries & Tools
- Lombok
- MapStruct 1.5.5
- Jakarta Bean Validation
- Jackson
- Maven

### Development Tools
- IntelliJ IDEA
- Postman
