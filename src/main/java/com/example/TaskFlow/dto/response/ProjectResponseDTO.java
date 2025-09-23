package com.example.TaskFlow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponseDTO {

    private Long id;
    private Long teamId;
    private String name;
    private String description;
    private Integer boardOrder;
    private Instant createdAt;
    private Instant updatedAt;
    private List<TaskSummaryDTO> tasks;
}
