package org.example.internshipassignmentkafka.controller;

import de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration;
import org.example.internshipassignmentkafka.enums.TaskPriority;
import org.example.internshipassignmentkafka.enums.TaskStatus;
import org.example.internshipassignmentkafka.dtos.TaskResponse;
import org.example.internshipassignmentkafka.service.KafkaClientService;
import org.example.internshipassignmentkafka.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.kafka.bootstrap-servers=kafka:9092",
                "spring.kafka.admin.properties.bootstrap.servers=kafka:9092",
                "spring.kafka.admin.auto-create=false",
                "spring.kafka.listener.auto-startup=false",
                "spring.mongodb.uri=mongodb://mongo:27017/TaskManagementSystem",
                "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/internship-task-realm"
        })
@EnableAutoConfiguration(exclude = {EmbeddedMongoAutoConfiguration.class})
class TaskControllerTest {

    @Autowired
    private ApplicationContext applicationContext;

    private WebTestClient webTestClient;

    @MockitoBean
    private KafkaClientService kafkaClientService;

    @MockitoBean
    private TaskService taskService;

    private TaskResponse aliceTaskResponse;
    private TaskResponse bobTaskResponse;

    @BeforeEach
    void setUpClient() {
        this.webTestClient = WebTestClient
                .bindToApplicationContext(applicationContext)
                .apply(SecurityMockServerConfigurers.springSecurity())
                .configureClient()
                .build();
    }

    @BeforeEach
    void setUp() {
        aliceTaskResponse = new TaskResponse(
                "507f1f77bcf86cd799439011",
                "TASK-1001",
                "Alice's task",
                "Test description",
                TaskStatus.PENDING,
                TaskPriority.HIGH,
                LocalDate.now().plusDays(7),
                LocalDateTime.now(),
                LocalDateTime.now(),
                "alice",
                "alice"
        );

        bobTaskResponse = new TaskResponse(
                "507f1f77bcf86cd799439012",
                "TASK-1002",
                "Bob's task",
                "Another description",
                TaskStatus.IN_PROGRESS,
                TaskPriority.MEDIUM,
                LocalDate.now().plusDays(3),
                LocalDateTime.now(),
                LocalDateTime.now(),
                "bob",
                "bob"
        );
    }

    // Valid role token
    private SecurityMockServerConfigurers.JwtMutator jwtFor(String username, String role) {
        return SecurityMockServerConfigurers.mockJwt()
                .jwt(jwt -> jwt
                        .subject(username)
                        .claim("preferred_username", username)
                        .claim("realm_access", Map.of("roles", List.of(role)))
                ).authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    // Token with no recognised role
    private SecurityMockServerConfigurers.JwtMutator jwtWithNoRole(String username) {
        return SecurityMockServerConfigurers.mockJwt()
                .jwt(jwt -> jwt
                        .subject(username)
                        .claim("preferred_username", username)
                        .claim("realm_access", Map.of("roles", List.of()))
                ).authorities(List.of());
    }

    @Nested
    @DisplayName("Global Security Configuration")
    class GlobalSecurityTests {

        @Test
        @DisplayName("Given no token, when requesting secured endpoints, then return 401 Unauthorized")
        void givenNoToken_whenRequestingSecuredEndpoints_thenReturn401() {
            webTestClient.get().uri("/api/tasks")
                    .exchange()
                    .expectStatus().isUnauthorized();
        }
    }

    @Nested
    @DisplayName("GET /api/tasks")
    class GetAllTasks {

        @BeforeEach
        void mockService() {
            when(taskService.getAllTasks(any())).thenReturn(Flux.just(aliceTaskResponse, bobTaskResponse));
            when(taskService.countAllTasks()).thenReturn(Mono.just(2L));
        }

        @Test
        @DisplayName("Given user has ADMIN role, when viewing all tasks, then return 200 OK")
        void givenAdminUser_whenViewingAllTasks_thenReturn200() {
            webTestClient.mutateWith(jwtFor("admin1", "ADMIN"))
                    .get().uri("/api/tasks")
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("Given user has USER role, when viewing all tasks, then return 200 OK")
        void givenNormalUser_whenViewingAllTasks_thenReturn200() {
            webTestClient.mutateWith(jwtFor("alice", "USER"))
                    .get().uri("/api/tasks")
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("Given token with no recognized role, when viewing all tasks, then return 403 Forbidden")
        void givenUserWithNoRole_whenViewingAllTasks_thenReturn403() {
            webTestClient.mutateWith(jwtWithNoRole("stranger"))
                    .get().uri("/api/tasks")
                    .exchange()
                    .expectStatus().isForbidden();
        }
    }

    @Nested
    @DisplayName("GET /api/tasks/{id}")
    class GetTaskById {

        @Test
        @DisplayName("Given user is ADMIN, when viewing any specific task, then return 200 OK")
        void givenAdmin_whenViewingAnyTask_thenReturn200() {
            when(taskService.getTask("TASK-1001")).thenReturn(Mono.just(aliceTaskResponse));

            webTestClient.mutateWith(jwtFor("admin1", "ADMIN"))
                    .get().uri("/api/tasks/TASK-1001")
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("Given user is normal USER, when viewing any specific task, then return 200 OK")
        void givenUser_whenViewingAnyTask_thenReturn200() {
            when(taskService.getTask("TASK-1001")).thenReturn(Mono.just(aliceTaskResponse));

            webTestClient.mutateWith(jwtFor("bob", "USER"))
                    .get().uri("/api/tasks/TASK-1001")
                    .exchange()
                    .expectStatus().isOk();
        }

        @Test
        @DisplayName("Given user has no recognized role, when viewing a specific task, then return 403 Forbidden")
        void givenNoRole_whenViewingTask_thenReturn403() {
            webTestClient.mutateWith(jwtWithNoRole("stranger"))
                    .get().uri("/api/tasks/TASK-1001")
                    .exchange()
                    .expectStatus().isForbidden();
        }
    }

    @Nested
    @DisplayName("POST /api/tasks")
    class CreateTask {

        private final String createPayload = """
                {
                  "title": "New Task",
                  "description": "A test task",
                  "status": "PENDING",
                  "priority": "LOW",
                  "dueDate": "2026-12-31"
                }
                """;

        @BeforeEach
        void mockKafka() {
            when(kafkaClientService.createTask(any())).thenReturn(Mono.empty());
        }

        @Test
        @DisplayName("Given ADMIN role, when creating a task, then return 202 Accepted")
        void givenAdmin_whenCreatingTask_thenReturn202() {
            webTestClient.mutateWith(jwtFor("admin1", "ADMIN"))
                    .post().uri("/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(createPayload)
                    .exchange()
                    .expectStatus().isAccepted();
        }

        @Test
        @DisplayName("Given USER role, when creating a task, then return 202 Accepted")
        void givenUser_whenCreatingTask_thenReturn202() {
            webTestClient.mutateWith(jwtFor("alice", "USER"))
                    .post().uri("/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(createPayload)
                    .exchange()
                    .expectStatus().isAccepted();
        }

        @Test
        @DisplayName("Given no recognized role, when creating a task, then return 403 Forbidden")
        void givenNoRole_whenCreatingTask_thenReturn403() {
            webTestClient.mutateWith(jwtWithNoRole("stranger"))
                    .post().uri("/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(createPayload)
                    .exchange()
                    .expectStatus().isForbidden();
        }
    }

    @Nested
    @DisplayName("PATCH /api/tasks/{id}")
    class UpdateTask {

        private final String updatePayload = """
                {
                  "title": "Updated Title",
                  "description": "Updated description",
                  "status": "IN_PROGRESS",
                  "priority": "HIGH",
                  "dueDate": "2026-12-31"
                }
                """;

        @Test
        @DisplayName("Given ADMIN user, when updating any task, then return 202 Accepted")
        void givenAdmin_whenUpdatingAnyTask_thenReturn202() {
            when(kafkaClientService.updateTask(eq("TASK-1002"), any())).thenReturn(Mono.empty());

            webTestClient.mutateWith(jwtFor("admin1", "ADMIN"))
                    .patch().uri("/api/tasks/TASK-1002")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(updatePayload)
                    .exchange()
                    .expectStatus().isAccepted();
        }

        @Test
        @DisplayName("Given task owner, when updating their own task, then return 202 Accepted")
        void givenTaskOwner_whenUpdatingTask_thenReturn202() {
            when(kafkaClientService.updateTask(eq("TASK-1001"), any())).thenReturn(Mono.empty());

            webTestClient.mutateWith(jwtFor("alice", "USER"))
                    .patch().uri("/api/tasks/TASK-1001")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(updatePayload)
                    .exchange()
                    .expectStatus().isAccepted();
        }

        @Test
        @DisplayName("Given normal USER, when updating another user's task, then return 403 Forbidden")
        void givenNonOwnerUser_whenUpdatingTask_thenReturn403() {
            // Mock the service layer throwing the AccessDeniedException due to ownership mismatch
            when(kafkaClientService.updateTask(eq("TASK-1001"), any()))
                    .thenReturn(Mono.error(new AccessDeniedException("You are not allowed to modify this task")));

            webTestClient.mutateWith(jwtFor("bob", "USER"))
                    .patch().uri("/api/tasks/TASK-1001")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(updatePayload)
                    .exchange()
                    .expectStatus().isForbidden();
        }

        @Test
        @DisplayName("Given no recognized role, when updating a task, then return 403 Forbidden")
        void givenNoRole_whenUpdatingTask_thenReturn403() {
            webTestClient.mutateWith(jwtWithNoRole("stranger"))
                    .patch().uri("/api/tasks/TASK-1001")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(updatePayload)
                    .exchange()
                    .expectStatus().isForbidden();
        }
    }

    @Nested
    @DisplayName("DELETE /api/tasks/{id}")
    class DeleteTask {

        @Test
        @DisplayName("Given ADMIN user, when deleting any task, then return 202 Accepted")
        void givenAdmin_whenDeletingTask_thenReturn202() {
            when(kafkaClientService.deleteTask("TASK-1002")).thenReturn(Mono.empty());

            webTestClient.mutateWith(jwtFor("admin1", "ADMIN"))
                    .delete().uri("/api/tasks/TASK-1002")
                    .exchange()
                    .expectStatus().isAccepted();
        }

        @Test
        @DisplayName("Given task owner, when deleting their task, then return 202 Accepted")
        void givenTaskOwner_whenDeletingTask_thenReturn202() {
            when(kafkaClientService.deleteTask("TASK-1001")).thenReturn(Mono.empty());

            webTestClient.mutateWith(jwtFor("alice", "USER"))
                    .delete().uri("/api/tasks/TASK-1001")
                    .exchange()
                    .expectStatus().isAccepted();
        }

        @Test
        @DisplayName("Given normal USER, when deleting another user's task, then return 403 Forbidden")
        void givenNonOwnerUser_whenDeletingTask_thenReturn403() {
            // Mock the service layer throwing the AccessDeniedException due to ownership mismatch
            when(kafkaClientService.deleteTask("TASK-1001"))
                    .thenReturn(Mono.error(new AccessDeniedException("You are not allowed to modify this task")));

            webTestClient.mutateWith(jwtFor("bob", "USER"))
                    .delete().uri("/api/tasks/TASK-1001")
                    .exchange()
                    .expectStatus().isForbidden();
        }

        @Test
        @DisplayName("Given no recognized role, when deleting a task, then return 403 Forbidden")
        void givenNoRole_whenDeletingTask_thenReturn403() {
            webTestClient.mutateWith(jwtWithNoRole("stranger"))
                    .delete().uri("/api/tasks/TASK-1001")
                    .exchange()
                    .expectStatus().isForbidden();
        }
    }
}