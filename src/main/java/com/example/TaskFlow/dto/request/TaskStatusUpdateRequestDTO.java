package com.example.TaskFlow.dto.request;

import com.example.TaskFlow.model.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record TaskStatusUpdateRequestDTO(@NotNull TaskStatus status) {
}
