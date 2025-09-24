package com.example.TaskFlow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectRequestDTO {

    @NotNull
    private Long teamId;

    @NotBlank
    @Size(max = 128)
    private String name;

    @Size(max = 1024)
    private String description;

    private LocalDate startDate;

    private LocalDate dueDate;
}
