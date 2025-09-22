package com.example.TaskFlow.dto.response;

import com.example.TaskFlow.Constants.Constants;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT access token issued by the TaskFlow authentication service.")
public record TokenResponse(
                @JsonProperty(Constants.ACCESS_TOKEN_CLAIM)
                @Schema(description = "JWT access token that must be sent using the Authorization header.",
                                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9")
                String accessToken
) {
}
