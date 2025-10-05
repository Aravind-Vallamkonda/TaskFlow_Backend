package com.example.TaskFlow.dto.request;

import com.example.TaskFlow.model.enums.TaskPriority;
import com.example.TaskFlow.model.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
public class TaskRequestDTO {

    @NotNull
    private Long projectId;

    @NotBlank
    @Size(max = 200)
    private String title;

    @Size(max = 4000)
    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private Long assigneeId;

    private Long delegateId;

    private Instant assignedAt;

    private Instant acknowledgedAt;

    private Instant completedAt;

    private LocalDate dueDate;

    private Integer sortOrder;

    private List<AttachmentPayloadDTO> attachments;

    private List<Long> removeAttachmentIds;
}
