package com.example.TaskFlow.controller;

import com.example.TaskFlow.dto.request.TeamCreateRequestDTO;
import com.example.TaskFlow.dto.request.TeamInviteRequestDTO;
import com.example.TaskFlow.dto.request.TeamInvitationResponseDTO;
import com.example.TaskFlow.dto.request.TeamRoleUpdateRequestDTO;
import com.example.TaskFlow.dto.response.TeamMembershipResponseDTO;
import com.example.TaskFlow.dto.response.TeamResponseDTO;
import com.example.TaskFlow.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Teams", description = "Operations for managing teams and memberships.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    @Operation(summary = "List teams", description = "Returns all teams the authenticated user belongs to.")
    public List<TeamResponseDTO> listTeams() {
        return teamService.getTeamsForUser(currentUsername());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    @Operation(summary = "Create a team", description = "Creates a new team with the authenticated user as administrator.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Team created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TeamResponseDTO.class)))
    })
    public ResponseEntity<TeamResponseDTO> createTeam(@Valid @RequestBody TeamCreateRequestDTO request) {
        TeamResponseDTO response = teamService.createTeam(request, currentUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{teamId}/invite")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Invite a member", description = "Invites a user to join the specified team.")
    public TeamResponseDTO inviteMember(@PathVariable Long teamId, @Valid @RequestBody TeamInviteRequestDTO request) {
        return teamService.inviteMember(teamId, request, currentUsername());
    }

    @PostMapping("/invitations/{membershipId}/respond")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    @Operation(summary = "Respond to invitation", description = "Accepts or declines a pending team invitation.")
    public TeamMembershipResponseDTO respondToInvitation(@PathVariable Long membershipId,
                                                         @Valid @RequestBody TeamInvitationResponseDTO request) {
        return teamService.respondToInvitation(membershipId, request, currentUsername());
    }

    @PutMapping("/{teamId}/members/{memberId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update member role", description = "Changes the role of a team member.")
    public TeamResponseDTO updateRole(@PathVariable Long teamId,
                                      @PathVariable Long memberId,
                                      @Valid @RequestBody TeamRoleUpdateRequestDTO request) {
        return teamService.updateMemberRole(teamId, memberId, request, currentUsername());
    }

    @DeleteMapping("/{teamId}/members/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    @Operation(summary = "Remove member", description = "Removes a member from the team or declines an invitation.")
    public ResponseEntity<Void> removeMember(@PathVariable Long teamId, @PathVariable Long memberId) {
        teamService.removeMember(teamId, memberId, currentUsername());
        return ResponseEntity.noContent().build();
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Authentication is required");
        }
        return authentication.getName();
    }
}
