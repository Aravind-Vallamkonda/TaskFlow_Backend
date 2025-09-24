package com.example.TaskFlow.dto.response;

import com.example.TaskFlow.model.enums.TaskPriority;
import com.example.TaskFlow.model.enums.TaskStatus;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Value
@Builder
public class TaskResponseDTO {
    Long id;
    Long projectId;
    String title;
    String description;
    TaskStatus status;
    TaskPriority priority;
    LocalDate dueDate;
    Integer sortOrder;
    TaskAssignmentDTO assignment;
    Instant createdAt;
    Instant updatedAt;
    List<CommentResponseDTO> comments;
    List<AttachmentResponseDTO> attachments;
}
