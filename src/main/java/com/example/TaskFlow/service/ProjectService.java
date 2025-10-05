package com.example.TaskFlow.service;

import com.example.TaskFlow.dto.mapper.ProjectMapper;
import com.example.TaskFlow.dto.request.ProjectRequestDTO;
import com.example.TaskFlow.dto.response.ProjectResponseDTO;
import com.example.TaskFlow.model.Project;
import com.example.TaskFlow.model.Team;
import com.example.TaskFlow.model.TeamMembership;
import com.example.TaskFlow.model.User;
import com.example.TaskFlow.repo.ProjectRepository;
import com.example.TaskFlow.repo.TeamMembershipRepository;
import com.example.TaskFlow.repo.TeamRepository;
import com.example.TaskFlow.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final TeamMembershipRepository teamMembershipRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;

    public ProjectService(ProjectRepository projectRepository,
                          TeamRepository teamRepository,
                          TeamMembershipRepository teamMembershipRepository,
                          UserRepository userRepository,
                          ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.teamRepository = teamRepository;
        this.teamMembershipRepository = teamMembershipRepository;
        this.userRepository = userRepository;
        this.projectMapper = projectMapper;
    }

    public List<ProjectResponseDTO> listProjects(Long teamId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));
        getActiveMembership(teamId, user.getId());
        return projectRepository.findByTeamId(team.getId()).stream()
                .map(projectMapper::toResponse)
                .toList();
    }

    public ProjectResponseDTO getProject(Long projectId, String username) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        getActiveMembership(project.getTeam().getId(), user.getId());
        return projectMapper.toResponse(project);
    }

    public ProjectResponseDTO createProject(ProjectRequestDTO request, String username) {
        validateDates(request.getStartDate(), request.getDueDate());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Team not found"));
        TeamMembership membership = getActiveMembership(team.getId(), user.getId());
        if (!membership.isActiveAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can manage projects");
        }
        String normalizedName = request.getName().trim();
        if (projectRepository.existsByTeamIdAndNameIgnoreCase(team.getId(), normalizedName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Project name already exists for team");
        }
        Project project = new Project();
        project.setTeam(team);
        projectMapper.applyUpsert(request, project);
        Project saved = projectRepository.save(project);
        return projectMapper.toResponse(saved);
    }

    public ProjectResponseDTO updateProject(Long projectId, ProjectRequestDTO request, String username) {
        validateDates(request.getStartDate(), request.getDueDate());
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        if (!project.getTeam().getId().equals(request.getTeamId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot move project between teams");
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        TeamMembership membership = getActiveMembership(project.getTeam().getId(), user.getId());
        if (!membership.isActiveAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can manage projects");
        }
        String normalizedName = request.getName().trim();
        if (projectRepository.existsByTeamIdAndNameIgnoreCase(project.getTeam().getId(), normalizedName)
                && !project.getName().equalsIgnoreCase(normalizedName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Project name already exists for team");
        }
        projectMapper.applyUpsert(request, project);
        return projectMapper.toResponse(project);
    }

    public void deleteProject(Long projectId, String username) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        TeamMembership membership = getActiveMembership(project.getTeam().getId(), user.getId());
        if (!membership.isActiveAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can manage projects");
        }
        projectRepository.delete(project);
    }

    private TeamMembership getActiveMembership(Long teamId, Long userId) {
        TeamMembership membership = teamMembershipRepository.findByTeam_IdAndUser_Id(teamId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a member of the team"));
        if (!membership.getStatus().isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Membership is not active");
        }
        return membership;
    }

    private void validateDates(LocalDate start, LocalDate due) {
        if (start != null && due != null && due.isBefore(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Due date cannot be before start date");
        }
    }
}
