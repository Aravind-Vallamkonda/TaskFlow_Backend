package com.example.TaskFlow.dto.mapper;

import com.example.TaskFlow.dto.response.ProjectResponseDTO;
import com.example.TaskFlow.dto.response.TeamMembershipResponseDTO;
import com.example.TaskFlow.dto.response.TeamResponseDTO;
import com.example.TaskFlow.model.Project;
import com.example.TaskFlow.model.Team;
import com.example.TaskFlow.model.TeamMembership;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class TeamMapper {

    public TeamResponseDTO toResponse(Team team) {
        TeamResponseDTO.TeamResponseDTOBuilder builder = TeamResponseDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt());

        team.getMemberships().stream()
                .sorted(Comparator.comparing(TeamMembership::getInvitedAt))
                .map(this::toMembershipResponse)
                .forEach(builder::member);

        team.getProjects().stream()
                .sorted(Comparator.comparing(Project::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toProjectResponse)
                .forEach(builder::project);

        return builder.build();
    }

    private TeamMembershipResponseDTO toMembershipResponse(TeamMembership membership) {
        return TeamMembershipResponseDTO.builder()
                .id(membership.getId())
                .userId(membership.getUser() != null ? membership.getUser().getId() : null)
                .username(membership.getUser() != null ? membership.getUser().getUsername() : null)
                .role(membership.getRole())
                .status(membership.getStatus())
                .invitedAt(membership.getInvitedAt())
                .joinedAt(membership.getJoinedAt())
                .respondedAt(membership.getRespondedAt())
                .build();
    }

    private ProjectResponseDTO toProjectResponse(Project project) {
        return ProjectResponseDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .dueDate(project.getDueDate())
                .build();
    }
}
