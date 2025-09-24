package com.example.TaskFlow.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Value
@Builder
public class TeamResponseDTO {
    Long id;
    String name;
    String description;
    Set<Long> memberIds;
    Instant createdAt;
    Instant updatedAt;
    List<ProjectResponseDTO> projects;
}
