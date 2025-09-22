package com.example.TaskFlow.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for updating the password of the authenticated user.")
public record ChangePasswordDTO(
                @Schema(description = "Current password used to authenticate the change.", example = "Sup3rSecret!")
                @NotBlank @Size(min = 8, max = 100) String oldPassword,

                @Schema(description = "New password that will replace the existing one.", example = "EvenB3tterSecret!")
                @NotBlank @Size(min = 8, max = 100) String newPassword
) {
}
