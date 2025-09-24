package com.example.TaskFlow.dto.response;

import com.example.TaskFlow.model.enums.MembershipStatus;
import com.example.TaskFlow.model.enums.TeamRole;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class TeamMembershipResponseDTO {
    Long id;
    Long userId;
    String username;
    TeamRole role;
    MembershipStatus status;
    Instant invitedAt;
    Instant joinedAt;
    Instant respondedAt;
}
