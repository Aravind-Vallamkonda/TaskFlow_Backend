package com.example.TaskFlow.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload used to initiate the multi-step authentication flow.")
public record IdentifyRequestDTO(
                @Schema(description = "Username or e-mail that uniquely identifies the user.", example = "alex.morgan")
                @NotBlank @Size(min = 4, max = 32) String identifier
) {
}
