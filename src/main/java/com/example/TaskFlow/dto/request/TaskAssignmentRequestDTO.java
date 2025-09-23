package com.example.TaskFlow.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TaskAssignmentRequestDTO {

    private Long assigneeId;
    private Long assignedById;
    private String notes;
}
