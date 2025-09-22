package com.example.TaskFlow.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Schema(name = "ApiError", description = "Standardised error payload returned by the TaskFlow API.")
public record ApiError(
                @Schema(description = "Moment when the error was generated.", example = "2024-10-28T09:32:15.123Z")
                Instant timestamp,

                @Schema(description = "HTTP status code of the response.", example = "400")
                int status,

                @Schema(description = "Short description matching the HTTP status phrase.", example = "Bad Request")
                String error,

                @Schema(description = "Detailed message that helps understanding the failure.", example = "Validation failed")
                String message,

                @Schema(description = "Request path that triggered the error.", example = "/auth/register")
                String path,

                @Schema(description = "Additional error context such as validation details.", example = "{\"username\":[\"must not be blank\"]}")
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
