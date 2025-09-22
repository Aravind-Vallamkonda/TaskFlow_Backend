package com.example.TaskFlow.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload that finalises the login flow by providing the flow identifier and password.")
public record LoginRequestDTO(
                @Schema(description = "Password that corresponds to the identified account.", example = "Sup3rSecret!")
                @NotBlank @Size(min = 8, max = 100) String password,

                @Schema(description = "Flow identifier returned by the identify endpoint.", example = "2e7829c7-9f6c-4d7d-82f9-2e89316b7466")
                @NotBlank String flowId
) {
}
