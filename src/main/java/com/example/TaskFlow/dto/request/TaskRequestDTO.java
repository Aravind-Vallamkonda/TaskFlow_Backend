package com.example.TaskFlow.dto.request;

import com.example.TaskFlow.model.enums.TaskPriority;
import com.example.TaskFlow.model.enums.TaskStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class TaskRequestDTO {

    private Long projectId;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate dueDate;
    private Integer columnPosition;
    private Integer swimlanePosition;
    private TaskAssignmentRequestDTO assignment;
}
