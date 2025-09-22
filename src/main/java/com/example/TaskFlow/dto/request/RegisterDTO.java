package com.example.TaskFlow.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload required to register a new TaskFlow user account.")
public record RegisterDTO(
                @Schema(description = "Unique username used for signing in.", example = "alex.morgan")
                @NotBlank @Size(min = 4, max = 50) String username,

                @Schema(description = "Valid e-mail address used for communication and recovery.", example = "alex.morgan@example.com")
                @Email @NotBlank String email,

                @Schema(description = "Password that satisfies the platform's complexity rules.", example = "Sup3rSecret!")
                @NotBlank @Size(min = 8, max = 100) String password,

                @Schema(description = "First name of the registrant.", example = "Alex")
                @NotBlank @Size(min = 1, max = 32) String firstname,

                @Schema(description = "Last name of the registrant.", example = "Morgan")
                @NotBlank @Size(min = 1, max = 32) String lastname
) {
}
