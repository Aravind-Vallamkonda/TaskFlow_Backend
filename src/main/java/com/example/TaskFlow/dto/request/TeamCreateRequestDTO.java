package com.example.TaskFlow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TeamCreateRequestDTO {
    @NotBlank
    @Size(max = 128)
    private String name;

    @Size(max = 512)
    private String description;
}
