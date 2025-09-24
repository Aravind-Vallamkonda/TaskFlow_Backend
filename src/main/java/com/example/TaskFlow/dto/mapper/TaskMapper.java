package com.example.TaskFlow.dto.mapper;

import com.example.TaskFlow.dto.request.AttachmentPayloadDTO;
import com.example.TaskFlow.dto.request.TaskRequestDTO;
import com.example.TaskFlow.dto.response.AttachmentResponseDTO;
import com.example.TaskFlow.dto.response.TaskAssignmentDTO;
import com.example.TaskFlow.dto.response.TaskResponseDTO;
import com.example.TaskFlow.model.Attachment;
import com.example.TaskFlow.model.Task;
import com.example.TaskFlow.model.User;
import com.example.TaskFlow.model.value.AssignmentMetadata;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class TaskMapper {

    public TaskResponseDTO toResponse(Task task) {
        AssignmentMetadata assignment = task.getAssignment();
        TaskAssignmentDTO assignmentDTO = null;
        if (assignment != null) {
            assignmentDTO = new TaskAssignmentDTO(
                    assignment.getAssignee() != null ? assignment.getAssignee().getId() : null,
                    assignment.getDelegate() != null ? assignment.getDelegate().getId() : null,
                    assignment.getAssignedAt(),
                    assignment.getAcknowledgedAt(),
                    assignment.getCompletedAt()
            );
        }

        List<AttachmentResponseDTO> attachments = task.getAttachments().stream()
                .map(this::toAttachmentResponse)
                .toList();

        return TaskResponseDTO.builder()
                .id(task.getId())
                .projectId(task.getProject() != null ? task.getProject().getId() : null)
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .sortOrder(task.getSortOrder())
                .assignment(assignmentDTO)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .comments(task.getComments().stream()
                        .map(comment -> com.example.TaskFlow.dto.response.CommentResponseDTO.builder()
                                .id(comment.getId())
                                .taskId(comment.getTask() != null ? comment.getTask().getId() : null)
                                .authorId(comment.getAuthor() != null ? comment.getAuthor().getId() : null)
                                .content(comment.getContent())
                                .createdAt(comment.getCreatedAt())
                                .updatedAt(comment.getUpdatedAt())
                                .build())
                        .toList())
                .attachments(attachments)
                .build();
    }

    public void applyUpsert(TaskRequestDTO dto, Task task, User assignee, User delegate) {
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        if (dto.getStatus() != null) {
            task.setStatus(dto.getStatus());
        }
        if (dto.getPriority() != null) {
            task.setPriority(dto.getPriority());
        }
        task.setDueDate(dto.getDueDate());
        task.setSortOrder(dto.getSortOrder());

        AssignmentMetadata assignment = task.getAssignment();
        if (assignment == null) {
            assignment = new AssignmentMetadata();
            task.setAssignment(assignment);
        }
        assignment.setAssignee(assignee);
        assignment.setDelegate(delegate);
        if (dto.getAssignedAt() != null) {
            assignment.setAssignedAt(dto.getAssignedAt());
        }
        if (dto.getAcknowledgedAt() != null) {
            assignment.setAcknowledgedAt(dto.getAcknowledgedAt());
        }
        if (dto.getCompletedAt() != null) {
            assignment.setCompletedAt(dto.getCompletedAt());
        }
    }

    public Attachment fromPayload(AttachmentPayloadDTO payload) {
        Attachment attachment = new Attachment();
        attachment.setFileName(payload.getFileName());
        attachment.setContentType(payload.getContentType());
        attachment.setStorageUrl(payload.getStorageUrl());
        attachment.setFileSize(payload.getFileSize());
        return attachment;
    }

    private AttachmentResponseDTO toAttachmentResponse(Attachment attachment) {
        return AttachmentResponseDTO.builder()
                .id(attachment.getId())
                .taskId(attachment.getTask() != null ? attachment.getTask().getId() : null)
                .fileName(attachment.getFileName())
                .contentType(attachment.getContentType())
                .storageUrl(attachment.getStorageUrl())
                .fileSize(attachment.getFileSize())
                .uploadedAt(attachment.getUploadedAt())
                .build();
    }

    public void mergeAttachments(Task task, List<AttachmentPayloadDTO> payloads, List<Long> removeIds) {
        if (removeIds != null && !removeIds.isEmpty()) {
            task.getAttachments().removeIf(att -> removeIds.contains(att.getId()));
        }
        if (payloads != null) {
            payloads.stream()
                    .filter(Objects::nonNull)
                    .map(this::fromPayload)
                    .forEach(attachment -> {
                        attachment.setTask(task);
                        task.getAttachments().add(attachment);
                    });
        }
    }
}
