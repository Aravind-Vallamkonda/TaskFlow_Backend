package com.example.TaskFlow.service;

import com.example.TaskFlow.dto.mapper.TeamMapper;
import com.example.TaskFlow.dto.request.TeamCreateRequestDTO;
import com.example.TaskFlow.dto.request.TeamInviteRequestDTO;
import com.example.TaskFlow.dto.request.TeamInvitationResponseDTO;
import com.example.TaskFlow.dto.request.TeamRoleUpdateRequestDTO;
import com.example.TaskFlow.dto.response.TeamMembershipResponseDTO;
import com.example.TaskFlow.dto.response.TeamResponseDTO;
import com.example.TaskFlow.model.Team;
import com.example.TaskFlow.model.TeamMembership;
import com.example.TaskFlow.model.User;
import com.example.TaskFlow.model.enums.MembershipStatus;
import com.example.TaskFlow.model.enums.TeamRole;
import com.example.TaskFlow.repo.TeamMembershipRepository;
import com.example.TaskFlow.repo.TeamRepository;
import com.example.TaskFlow.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class TeamService {

    private static final Logger logger = LoggerFactory.getLogger(TeamService.class);

    private final TeamRepository teamRepository;
    private final TeamMembershipRepository teamMembershipRepository;
    private final UserRepository userRepository;
    private final TeamMapper teamMapper;

    public TeamService(TeamRepository teamRepository,
                       TeamMembershipRepository teamMembershipRepository,
                       UserRepository userRepository,
                       TeamMapper teamMapper) {
        this.teamRepository = teamRepository;
        this.teamMembershipRepository = teamMembershipRepository;
        this.userRepository = userRepository;
        this.teamMapper = teamMapper;
    }

    public TeamResponseDTO createTeam(TeamCreateRequestDTO request, String creatorUsername) {
        User creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (teamRepository.findByName(request.getName().trim()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Team name already in use");
        }
        Team team = new Team();
        team.setName(request.getName().trim());
        team.setDescription(request.getDescription());
        TeamMembership membership = team.addMembership(creator, TeamRole.ADMIN);
        membership.setInvitedBy(creator);
        membership.setInvitedAt(Instant.now());
        Team persisted = teamRepository.save(team);
        return teamMapper.toResponse(persisted);
    }

    @Transactional
    public List<TeamResponseDTO> getTeamsForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return teamRepository.findDistinctByMembershipsUserIdAndMembershipsStatus(user.getId(), MembershipStatus.ACTIVE)
                .stream()
                .map(teamMapper::toResponse)
                .toList();
    }

    public TeamResponseDTO inviteMember(Long teamId, TeamInviteRequestDTO request, String inviterUsername) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));
        User inviter = userRepository.findByUsername(inviterUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        TeamMembership inviterMembership = teamMembershipRepository.findByTeam_IdAndUser_Id(teamId, inviter.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member of the team"));
        if (!inviterMembership.isActiveAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can invite members");
        }
        User invitee = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitee not found"));
        TeamMembership existing = teamMembershipRepository.findByTeam_IdAndUser_Id(teamId, invitee.getId()).orElse(null);
        if (existing != null) {
            logger.info("Team MemberShip : "+existing);
            switch (existing.getStatus()) {
                case ACTIVE -> throw new ResponseStatusException(HttpStatus.CONFLICT, "User already an active member");
                case INVITED -> {
                    existing.setRole(request.getRole() != null ? request.getRole() : TeamRole.MEMBER);
                    existing.setInvitedBy(inviter);
                    existing.setInvitedAt(Instant.now());
                    teamMembershipRepository.save(existing);
                    return teamMapper.toResponse(team);
                }
                default -> {
                    existing.setStatus(MembershipStatus.INVITED);
                    existing.setRole(request.getRole() != null ? request.getRole() : TeamRole.MEMBER);
                    existing.setInvitedBy(inviter);
                    existing.setInvitedAt(Instant.now());
                    existing.setRespondedAt(null);
                    existing.setJoinedAt(null);
                    teamMembershipRepository.save(existing);
                    return teamMapper.toResponse(team);
                }
            }
        }
        TeamMembership membership = new TeamMembership();
        membership.setTeam(team);
        logger.info(invitee.toString());
        membership.setUser(invitee);
        membership.setRole(request.getRole() != null ? request.getRole() : TeamRole.MEMBER);
        membership.setInvitedBy(inviter);
        membership.setStatus(MembershipStatus.INVITED);
        membership.setInvitedAt(Instant.now());
        team.getMemberships().add(membership);
        invitee.getMemberships().add(membership);
        teamMembershipRepository.save(membership);
        return teamMapper.toResponse(team);
    }

    public TeamMembershipResponseDTO respondToInvitation(Long membershipId, TeamInvitationResponseDTO response, String username) {
        TeamMembership membership = teamMembershipRepository.findById(membershipId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));
        if (!Objects.equals(membership.getUser() != null ? membership.getUser().getUsername() : null, username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot respond to invitations for other users");
        }
        if (membership.getStatus() != MembershipStatus.INVITED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invitation already processed");
        }
        if (Boolean.TRUE.equals(response.accept())) {
            membership.activate();
        } else {
            membership.decline();
        }
        teamMembershipRepository.save(membership);
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

    public TeamResponseDTO updateMemberRole(Long teamId, Long memberId, TeamRoleUpdateRequestDTO request, String actorUsername) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));
        User actor = userRepository.findByUsername(actorUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        TeamMembership actorMembership = teamMembershipRepository.findByTeam_IdAndUser_Id(teamId, actor.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a team member"));
        if (!actorMembership.isActiveAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can update roles");
        }
        TeamMembership target = teamMembershipRepository.findByTeam_IdAndUser_Id(teamId, memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membership not found"));
        if (!target.getStatus().isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Membership is not active");
        }
        target.setRole(request.role());
        teamMembershipRepository.save(target);
        return teamMapper.toResponse(team);
    }

    public void removeMember(Long teamId, Long memberId, String actorUsername) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));
        User actor = userRepository.findByUsername(actorUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        TeamMembership actorMembership = teamMembershipRepository.findByTeam_IdAndUser_Id(teamId, actor.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a team member"));
        if (!actorMembership.isActiveAdmin() && !Objects.equals(memberId, actor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permissions to remove members");
        }
        TeamMembership target = teamMembershipRepository.findByTeam_IdAndUser_Id(teamId, memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Membership not found"));
        if (!target.getStatus().isActive()) {
            target.setStatus(MembershipStatus.REMOVED);
            teamMembershipRepository.save(target);
            return;
        }
        long adminCount = team.getMemberships().stream().filter(TeamMembership::isActiveAdmin).count();
        if (target.isActiveAdmin() && adminCount <= 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot remove the last administrator");
        }
        target.setStatus(MembershipStatus.REMOVED);
        target.setRespondedAt(Instant.now());
        target.setJoinedAt(null);
        teamMembershipRepository.save(target);
    }
}
