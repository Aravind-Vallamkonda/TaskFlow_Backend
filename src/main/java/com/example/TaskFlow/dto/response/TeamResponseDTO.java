package com.example.TaskFlow.dto.response;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class TeamResponseDTO {
    Long id;
    String name;
    String description;
    Instant createdAt;
    Instant updatedAt;
    @Singular
    List<TeamMembershipResponseDTO> members;
    @Singular
    List<ProjectResponseDTO> projects;
}
