package com.example.TaskFlow.dto.response;

import com.example.TaskFlow.model.enums.TaskPriority;
import com.example.TaskFlow.model.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponseDTO {

    private Long id;
    private Long projectId;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDate dueDate;
    private Integer columnPosition;
    private Integer swimlanePosition;
    private TaskAssignmentResponseDTO assignment;
    private Instant createdAt;
    private Instant updatedAt;
    private List<CommentResponseDTO> comments;
    private List<AttachmentResponseDTO> attachments;
}
