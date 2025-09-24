package com.example.TaskFlow.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class TaskAssignmentDTO {
    Long assigneeId;
    Long delegateId;
    Instant assignedAt;
    Instant acknowledgedAt;
    Instant completedAt;
}
