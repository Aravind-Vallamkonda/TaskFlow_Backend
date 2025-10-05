package com.example.TaskFlow.service;

import com.example.TaskFlow.dto.mapper.ProjectMapper;
import com.example.TaskFlow.dto.mapper.TaskMapper;
import com.example.TaskFlow.dto.request.ProjectRequestDTO;
import com.example.TaskFlow.dto.response.ProjectResponseDTO;
import com.example.TaskFlow.model.Project;
import com.example.TaskFlow.model.Team;
import com.example.TaskFlow.model.TeamMembership;
import com.example.TaskFlow.model.User;
import com.example.TaskFlow.model.enums.MembershipStatus;
import com.example.TaskFlow.model.enums.TeamRole;
import com.example.TaskFlow.repo.ProjectRepository;
import com.example.TaskFlow.repo.TeamMembershipRepository;
import com.example.TaskFlow.repo.TeamRepository;
import com.example.TaskFlow.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private TeamMembershipRepository teamMembershipRepository;
    @Mock
    private UserRepository userRepository;

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(projectRepository, teamRepository, teamMembershipRepository, userRepository, new ProjectMapper(new TaskMapper()));
    }

    @Test
    void createProjectShouldPersistWhenAdmin() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        Team team = new Team();
        team.setId(2L);
        TeamMembership membership = new TeamMembership();
        membership.setTeam(team);
        membership.setUser(user);
        membership.setRole(TeamRole.ADMIN);
        membership.setStatus(MembershipStatus.ACTIVE);

        ProjectRequestDTO request = new ProjectRequestDTO();
        request.setTeamId(2L);
        request.setName("Launch Pad");
        request.setStartDate(LocalDate.now());
        request.setDueDate(LocalDate.now().plusDays(7));

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(teamRepository.findById(2L)).thenReturn(Optional.of(team));
        when(teamMembershipRepository.findByTeam_IdAndUser_Id(2L, 1L)).thenReturn(Optional.of(membership));
        when(projectRepository.existsByTeamIdAndNameIgnoreCase(2L, "Launch Pad")).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(10L);
            return project;
        });

        ProjectResponseDTO response = projectService.createProject(request, "admin");

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getTeamId()).isEqualTo(2L);
        assertThat(response.getName()).isEqualTo("Launch Pad");
    }

    @Test
    void createProjectShouldRejectNonAdmin() {
        User user = new User();
        user.setId(1L);
        user.setUsername("member");
        Team team = new Team();
        team.setId(2L);
        TeamMembership membership = new TeamMembership();
        membership.setTeam(team);
        membership.setUser(user);
        membership.setRole(TeamRole.MEMBER);
        membership.setStatus(MembershipStatus.ACTIVE);

        ProjectRequestDTO request = new ProjectRequestDTO();
        request.setTeamId(2L);
        request.setName("Iteration Zero");

        when(userRepository.findByUsername("member")).thenReturn(Optional.of(user));
        when(teamRepository.findById(2L)).thenReturn(Optional.of(team));
        when(teamMembershipRepository.findByTeam_IdAndUser_Id(2L, 1L)).thenReturn(Optional.of(membership));

        assertThatThrownBy(() -> projectService.createProject(request, "member"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only administrators can manage projects");
    }

    @Test
    void updateProjectShouldValidateTeamConsistency() {
        Project project = new Project();
        Team team = new Team();
        team.setId(3L);
        project.setTeam(team);

        ProjectRequestDTO request = new ProjectRequestDTO();
        request.setTeamId(4L);
        request.setName("Mismatch");

        when(projectRepository.findById(5L)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.updateProject(5L, request, "admin"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Cannot move project between teams");
    }

    @Test
    void deleteProjectShouldRequireAdministrator() {
        Project project = new Project();
        Team team = new Team();
        team.setId(8L);
        project.setTeam(team);
        User user = new User();
        user.setId(9L);
        user.setUsername("member");
        TeamMembership membership = new TeamMembership();
        membership.setTeam(team);
        membership.setUser(user);
        membership.setRole(TeamRole.MEMBER);
        membership.setStatus(MembershipStatus.ACTIVE);

        when(projectRepository.findById(12L)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("member")).thenReturn(Optional.of(user));
        when(teamMembershipRepository.findByTeam_IdAndUser_Id(8L, 9L)).thenReturn(Optional.of(membership));

        assertThatThrownBy(() -> projectService.deleteProject(12L, "member"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only administrators can manage projects");
    }
}
