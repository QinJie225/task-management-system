package org.example.internshipassignmentkafka.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleNotFound(
            TaskNotFoundException ex,
            ServerHttpRequest request
    ) {
        log.warn("{}", ex.getMessage());
        return Mono.just(ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.NOT_FOUND.value(),
                        "Not Found",
                        ex.getMessage(),
                        request.getPath().value(),
                        null
                )));
    }

    // WebFlux equivalent of MethodArgumentNotValidException
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleValidation(
            WebExchangeBindException ex,
            ServerHttpRequest request
    ) {
        List<FieldErrorDto> fieldErrors = ex.getFieldErrors()
                .stream()
                .map(fe -> new FieldErrorDto(fe.getField(), fe.getDefaultMessage()))
                .toList();

        log.warn(
                "Validation failed. Endpoint: {}, Method: {}, Errors: {}",
                request.getPath().value(),
                request.getMethod(),
                fieldErrors
        );

        return Mono.just(ResponseEntity
                .badRequest()
                .body(new ApiErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation Failed",
                        "Request contains invalid fields",
                        request.getPath().value(),
                        fieldErrors
                )));
    }

    // WebFlux equivalent of HttpMessageNotReadableException
    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleInvalidInput(
            ServerWebInputException ex,
            ServerHttpRequest request
    ) {
        String message = buildMessage(ex);
        log.warn("Invalid request body: {}", ex.getMessage());
        return Mono.just(ResponseEntity
                .badRequest()
                .body(new ApiErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "Bad Request",
                        message,
                        request.getPath().value(),
                        null
                )));
    }

    @ExceptionHandler(EmptyUpdateRequestException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleBadRequest(
            EmptyUpdateRequestException ex,
            ServerHttpRequest request
    ) {
        log.warn("{}", ex.getMessage());
        return Mono.just(ResponseEntity
                .badRequest()
                .body(new ApiErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "Bad Request",
                        ex.getMessage(),
                        request.getPath().value(),
                        null
                )));
    }

    @ExceptionHandler(KafkaPublishFailedException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleKafkaPublishFailed(
            KafkaPublishFailedException ex,
            ServerHttpRequest request
    ) {
        log.error("Kafka publish failure: {}", ex.getMessage());
        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        ex.getMessage(),
                        request.getPath().value(),
                        null
                )));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleForbidden(
            AccessDeniedException ex,
            ServerHttpRequest request
    ) {
        log.warn("Access denied for path {}: {}", request.getPath().value(), ex.getMessage());
        return Mono.just(ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.FORBIDDEN.value(),
                        "Forbidden",
                        ex.getMessage(),
                        request.getPath().value(),
                        null
                )));
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public Mono<ResponseEntity<ApiErrorResponse>> handleUnauthorized(
            org.springframework.security.core.AuthenticationException ex,
            ServerHttpRequest request
    ) {
        log.warn("Unauthorized access to {}: {}", request.getPath().value(), ex.getMessage());
        return Mono.just(ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.UNAUTHORIZED.value(),
                        "Unauthorized",
                        "Missing or invalid authentication token",
                        request.getPath().value(),
                        null
                )));
    }

    private String buildMessage(Throwable ex) {
        Throwable t = ex;
        while (t != null) {
            if (t.getClass().getName().endsWith("InvalidFormatException")) {
                Class<?> targetType = extractTargetType(t);
                if (targetType != null && targetType.isEnum()) {
                    String allowedValues = Arrays.stream(targetType.getEnumConstants())
                            .map(Object::toString)
                            .collect(Collectors.joining(", "));
                    return "Invalid value. Allowed values: " + allowedValues;
                }
            }
            if (t instanceof TypeMismatchException tme) {
                Class<?> targetType = tme.getRequiredType();
                if (targetType != null && targetType.isEnum()) {
                    return buildEnumErrorMessage(targetType);
                }
            }
            t = t.getCause();
        }
        return "Invalid request format";
    }

    private String buildEnumErrorMessage(Class<?> enumType) {
        String allowedValues = Arrays.stream(enumType.getEnumConstants())
                .map(Object::toString)
                .collect(Collectors.joining(", "));
        return "Invalid value. Allowed values: " + allowedValues;
    }

    private Class<?> extractTargetType(Throwable t) {
        try {
            return (Class<?>) t.getClass().getMethod("getTargetType").invoke(t);
        } catch (Exception e) {
            return null;
        }
    }
}