package com.example.TaskFlow.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Value
@Builder
public class ProjectResponseDTO {
    Long id;
    Long teamId;
    String name;
    String description;
    LocalDate startDate;
    LocalDate dueDate;
    Instant createdAt;
    Instant updatedAt;
    List<TaskResponseDTO> tasks;
}
