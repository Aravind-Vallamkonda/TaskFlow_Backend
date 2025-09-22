package com.example.TaskFlow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Wrapper containing a human readable message about the result of an API call.")
public record ApiMessageResponse(
                @Schema(description = "Summary of the outcome.", example = "User created successfully.")
                String message
) {
}
