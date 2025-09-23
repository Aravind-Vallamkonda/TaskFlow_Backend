package com.example.TaskFlow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAssignmentResponseDTO {

    private Long assigneeId;
    private Long assignedById;
    private Instant assignedAt;
    private String notes;
}
