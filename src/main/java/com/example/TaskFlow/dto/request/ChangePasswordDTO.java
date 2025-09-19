package com.example.TaskFlow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordDTO(
        @NotBlank @Size(min = 8, max = 100) String oldPassword,
        @NotBlank @Size(min= 8, max =100) String newPassword
        ) {

}
