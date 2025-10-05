package com.example.TaskFlow.dto.request;

import jakarta.validation.constraints.NotNull;

public record TeamInvitationResponseDTO(@NotNull Boolean accept) {
}
