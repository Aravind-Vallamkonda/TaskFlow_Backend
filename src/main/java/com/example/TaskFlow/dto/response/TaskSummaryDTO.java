package com.example.TaskFlow.dto.response;

import com.example.TaskFlow.model.enums.TaskPriority;
import com.example.TaskFlow.model.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskSummaryDTO {

    private Long id;
    private String title;
    private TaskStatus status;
    private TaskPriority priority;
    private Integer columnPosition;
    private Integer swimlanePosition;
}
