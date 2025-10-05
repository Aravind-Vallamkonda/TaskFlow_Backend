package com.example.TaskFlow.dto.request;

import com.example.TaskFlow.model.enums.TeamRole;
import jakarta.validation.constraints.NotNull;

public record TeamRoleUpdateRequestDTO(@NotNull TeamRole role) {
}
