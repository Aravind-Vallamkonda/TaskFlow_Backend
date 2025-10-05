package com.example.TaskFlow.controller;

import com.example.TaskFlow.dto.request.ProjectRequestDTO;
import com.example.TaskFlow.dto.response.ProjectResponseDTO;
import com.example.TaskFlow.service.ProjectService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Projects", description = "Endpoints for managing projects within teams.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    @Operation(summary = "List projects", description = "Lists projects for the specified team.")
    public List<ProjectResponseDTO> listProjects(@RequestParam Long teamId, Authentication authentication) {
        return projectService.listProjects(teamId, authentication.getName());
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    @Operation(summary = "Get a project", description = "Fetches a single project with its metadata.")
    public ProjectResponseDTO getProject(@PathVariable Long projectId, Authentication authentication) {
        return projectService.getProject(projectId, authentication.getName());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    @Operation(summary = "Create a project", description = "Creates a new project for a team.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Project created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResponseDTO.class)))
    })
    public ResponseEntity<ProjectResponseDTO> createProject(@Valid @RequestBody ProjectRequestDTO request,
                                                            Authentication authentication) {
        ProjectResponseDTO response = projectService.createProject(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    @Operation(summary = "Update a project", description = "Updates the details of an existing project.")
    public ProjectResponseDTO updateProject(@PathVariable Long projectId,
                                            @Valid @RequestBody ProjectRequestDTO request,
                                            Authentication authentication) {
        return projectService.updateProject(projectId, request, authentication.getName());
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    @Operation(summary = "Delete a project", description = "Deletes a project and its tasks.")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId, Authentication authentication) {
        projectService.deleteProject(projectId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
