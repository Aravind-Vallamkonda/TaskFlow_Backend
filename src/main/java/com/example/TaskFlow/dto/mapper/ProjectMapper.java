package com.example.TaskFlow.dto.mapper;

import com.example.TaskFlow.dto.request.ProjectRequestDTO;
import com.example.TaskFlow.dto.response.ProjectResponseDTO;
import com.example.TaskFlow.model.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    private final TaskMapper taskMapper;

    @Autowired
    public ProjectMapper(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    public ProjectResponseDTO toResponse(Project project) {
        return ProjectResponseDTO.builder()
                .id(project.getId())
                .teamId(project.getTeam() != null ? project.getTeam().getId() : null)
                .name(project.getName())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .dueDate(project.getDueDate())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .tasks(project.getTasks() != null ? project.getTasks().stream()
                        .map(taskMapper::toResponse)
                        .toList() : null)
                .build();
    }

    public void applyUpsert(ProjectRequestDTO request, Project project) {
        project.setName(request.getName().trim());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setDueDate(request.getDueDate());
    }
}
