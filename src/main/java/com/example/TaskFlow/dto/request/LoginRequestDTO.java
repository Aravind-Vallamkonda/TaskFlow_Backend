package com.example.TaskFlow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/*
@Data
public class LoginRequestDTO {

    @NotBlank
    @Size(min = 8, max =100)
    private String password;
    @NotBlank
    private String flowId;
}
*/

public record LoginRequestDTO(
        @NotBlank @Size(min = 8, max =100) String password,
        @NotBlank String flowId
){}