package com.example.TaskFlow.service;

import com.example.TaskFlow.dto.mapper.TeamMapper;
import com.example.TaskFlow.dto.request.TeamCreateRequestDTO;
import com.example.TaskFlow.dto.request.TeamInviteRequestDTO;
import com.example.TaskFlow.dto.request.TeamRoleUpdateRequestDTO;
import com.example.TaskFlow.dto.response.TeamResponseDTO;
import com.example.TaskFlow.model.Team;
import com.example.TaskFlow.model.TeamMembership;
import com.example.TaskFlow.model.User;
import com.example.TaskFlow.model.enums.MembershipStatus;
import com.example.TaskFlow.model.enums.TeamRole;
import com.example.TaskFlow.repo.TeamMembershipRepository;
import com.example.TaskFlow.repo.TeamRepository;
import com.example.TaskFlow.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;
    @Mock
    private TeamMembershipRepository membershipRepository;
    @Mock
    private UserRepository userRepository;

    private TeamService teamService;

    @BeforeEach
    void setUp() {
        teamService = new TeamService(teamRepository, membershipRepository, userRepository, new TeamMapper());
    }

    @Test
    void createTeamAssignsCreatorAsAdmin() {
        User creator = new User();
        creator.setId(5L);
        creator.setUsername("creator");
        when(userRepository.findByUsername("creator")).thenReturn(Optional.of(creator));
        when(teamRepository.findByName("Platform")).thenReturn(Optional.empty());
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TeamCreateRequestDTO request = new TeamCreateRequestDTO();
        request.setName("Platform");
        request.setDescription("Platform engineering");

        TeamResponseDTO response = teamService.createTeam(request, "creator");

        assertThat(response.getName()).isEqualTo("Platform");
        assertThat(response.getMembers()).anyMatch(member -> member.getUserId().equals(5L) && member.getRole() == TeamRole.ADMIN);
    }

    @Test
    void inviteMemberRequiresAdminRole() {
        Team team = new Team();
        team.setId(4L);
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        TeamMembership adminMembership = new TeamMembership();
        adminMembership.setTeam(team);
        adminMembership.setUser(admin);
        adminMembership.setRole(TeamRole.ADMIN);
        adminMembership.setStatus(MembershipStatus.ACTIVE);
        when(teamRepository.findById(4L)).thenReturn(Optional.of(team));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(membershipRepository.findByTeam_IdAndUser_Id(4L, 1L)).thenReturn(Optional.of(adminMembership));
        User invitee = new User();
        invitee.setId(8L);
        invitee.setUsername("member");
        when(userRepository.findById(8L)).thenReturn(Optional.of(invitee));
        when(teamRepository.save(any(Team.class))).thenReturn(team);

        TeamInviteRequestDTO request = new TeamInviteRequestDTO();
        request.setUserId(8L);
        request.setRole(TeamRole.MEMBER);

        TeamResponseDTO response = teamService.inviteMember(4L, request, "admin");
        assertThat(response.getMembers()).anyMatch(member -> member.getUserId().equals(8L) && member.getStatus() == MembershipStatus.INVITED);
    }

    @Test
    void updateRoleValidatesActiveMembership() {
        Team team = new Team();
        team.setId(2L);
        User admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        TeamMembership adminMembership = new TeamMembership();
        adminMembership.setTeam(team);
        adminMembership.setUser(admin);
        adminMembership.setRole(TeamRole.ADMIN);
        adminMembership.setStatus(MembershipStatus.ACTIVE);
        User member = new User();
        member.setId(7L);
        member.setUsername("member");
        TeamMembership memberMembership = new TeamMembership();
        memberMembership.setTeam(team);
        memberMembership.setUser(member);
        memberMembership.setStatus(MembershipStatus.ACTIVE);
        memberMembership.setRole(TeamRole.MEMBER);
        when(teamRepository.findById(2L)).thenReturn(Optional.of(team));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(membershipRepository.findByTeam_IdAndUser_Id(2L, 1L)).thenReturn(Optional.of(adminMembership));
        when(membershipRepository.findByTeam_IdAndUser_Id(2L, 7L)).thenReturn(Optional.of(memberMembership));

        TeamRoleUpdateRequestDTO request = new TeamRoleUpdateRequestDTO(TeamRole.ADMIN);
        teamService.updateMemberRole(2L, 7L, request, "admin");

        assertThat(memberMembership.getRole()).isEqualTo(TeamRole.ADMIN);
    }

    @Test
    void removeMemberPreventsRemovingLastAdmin() {
        Team team = new Team();
        team.setId(10L);
        User admin = new User();
        admin.setId(3L);
        admin.setUsername("admin");
        TeamMembership adminMembership = new TeamMembership();
        adminMembership.setTeam(team);
        adminMembership.setUser(admin);
        adminMembership.setRole(TeamRole.ADMIN);
        adminMembership.setStatus(MembershipStatus.ACTIVE);
        team.getMemberships().add(adminMembership);
        when(teamRepository.findById(10L)).thenReturn(Optional.of(team));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(membershipRepository.findByTeam_IdAndUser_Id(10L, 3L)).thenReturn(Optional.of(adminMembership));
        when(membershipRepository.findByTeam_IdAndUser_Id(10L, 3L)).thenReturn(Optional.of(adminMembership));

        assertThatThrownBy(() -> teamService.removeMember(10L, 3L, "admin"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Cannot remove the last administrator");
    }
}
