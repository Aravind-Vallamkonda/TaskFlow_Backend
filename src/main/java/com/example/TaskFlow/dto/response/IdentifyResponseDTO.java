package com.example.TaskFlow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing the flow identifier that must be used for the next login step.")
public record IdentifyResponseDTO(
                @Schema(description = "Unique flow identifier valid for a short period of time.", example = "2e7829c7-9f6c-4d7d-82f9-2e89316b7466")
                String flowId
) {
}
