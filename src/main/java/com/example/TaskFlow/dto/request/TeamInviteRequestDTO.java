package com.example.TaskFlow.dto.request;

import com.example.TaskFlow.model.enums.TeamRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TeamInviteRequestDTO {
    @NotNull
    private Long userId;

    private TeamRole role = TeamRole.MEMBER;
}
