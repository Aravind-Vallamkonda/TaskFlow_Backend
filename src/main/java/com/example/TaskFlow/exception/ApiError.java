package com.example.TaskFlow.exception;

import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple DTO used to render structured error responses for the REST API.
 * <p>
 * The record keeps a timestamp, HTTP status information and optional details that
 * callers can use to display validation errors or extra context about the failure.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, ?> details
) {

    private static final Map<String, ?> EMPTY_DETAILS = Map.of();

    public ApiError {
        if (message == null || message.isBlank()) {
            message = "Unexpected error";
        }
        if (path == null) {
            path = "";
        }
        if (details == null || details.isEmpty()) {
            details = EMPTY_DETAILS;
        } else {
            Map<String, Object> copy = new LinkedHashMap<>();
            details.forEach(copy::put);
            details = Collections.unmodifiableMap(copy);
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }

    public static ApiError of(HttpStatus status, String message, String path) {
        return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, path, EMPTY_DETAILS);
    }

    public static ApiError of(HttpStatus status, String message, String path, Map<String, ?> details) {
        if (details == null || details.isEmpty()) {
            return of(status, message, path);
        }
        return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, path, details);
    }
}
