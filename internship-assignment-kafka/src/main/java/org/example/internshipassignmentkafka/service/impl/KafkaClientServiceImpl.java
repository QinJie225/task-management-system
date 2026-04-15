package org.example.internshipassignmentkafka.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.internshipassignmentkafka.dtos.CreateTaskRequest;
import org.example.internshipassignmentkafka.dtos.UpdateTaskRequest;
import org.example.internshipassignmentkafka.exception.TaskNotFoundException;
import org.example.internshipassignmentkafka.kafka.TaskEventProducer;
import org.example.internshipassignmentkafka.repository.TaskRepository;
import org.example.internshipassignmentkafka.service.KafkaClientService;
import org.example.internshipassignmentkafka.utility.SecurityUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaClientServiceImpl implements KafkaClientService {

    private final TaskEventProducer taskEventProducer;
    private final TaskRepository taskRepository;
    private final SecurityUtils securityUtils;

    private Mono<String> verifyOwnershipOrAdminAndGetUsername(String taskId) {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .cast(JwtAuthenticationToken.class)
                .flatMap(auth -> {
                    String username = auth.getToken().getClaimAsString("preferred_username");
                    boolean isAdmin = auth.getAuthorities().stream()
                            .anyMatch(grantedAuth -> grantedAuth.getAuthority().equals("ROLE_ADMIN"));

                    return taskRepository.findByTaskId(taskId)
                            .switchIfEmpty(Mono.error(new TaskNotFoundException(taskId)))
                            .flatMap(task -> {
                                // If Admin OR if the username matches the creator
                                if (isAdmin || (username != null && username.equals(task.getCreatedBy()))) {
                                    return Mono.just(username != null ? username : "system");
                                } else {
                                    return Mono.error(new AccessDeniedException("You are not allowed to modify this task"));
                                }
                            });
                });
    }

    @Override
    public Mono<Void> createTask(CreateTaskRequest request) {
        return securityUtils.getCurrentUsername()
                .flatMap(username -> taskEventProducer.publishCreateEvent(request, username))
                .doOnSuccess(v -> log.info("Published TASK_CREATED event for title={}", request.title()));
    }

    @Override
    public Mono<Void> updateTask(String taskId, UpdateTaskRequest request) {
        return verifyOwnershipOrAdminAndGetUsername(taskId)
                .flatMap(username -> taskEventProducer.publishUpdateEvent(taskId, request, username))
                .doOnSuccess(v -> log.info("Published TASK_UPDATED event for taskId={}", taskId));
    }

    @Override
    public Mono<Void> deleteTask(String taskId) {
        return verifyOwnershipOrAdminAndGetUsername(taskId)
                .flatMap(username -> taskEventProducer.publishDeleteEvent(taskId))
                .doOnSuccess(v -> log.info("Published TASK_DELETED event for taskId={}", taskId));
    }
}