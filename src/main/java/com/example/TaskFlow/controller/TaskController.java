package com.example.TaskFlow.controller;

import com.example.TaskFlow.dto.request.TaskReorderRequestDTO;
import com.example.TaskFlow.dto.request.TaskRequestDTO;
import com.example.TaskFlow.dto.request.TaskStatusUpdateRequestDTO;
import com.example.TaskFlow.dto.response.TaskResponseDTO;
import com.example.TaskFlow.model.enums.TaskStatus;
import com.example.TaskFlow.service.TaskService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Tag(name = "Tasks", description = "Endpoints for managing tasks within TaskFlow projects.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    @Operation(summary = "List tasks", description = "Fetches tasks with optional filters for project, status and assignee.")
    public List<TaskResponseDTO> listTasks(@RequestParam(required = false) Long projectId,
                                           @RequestParam(required = false) TaskStatus status,
                                           @RequestParam(required = false) Long assigneeId) {
        return taskService.getTasks(projectId, Optional.ofNullable(status), Optional.ofNullable(assigneeId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    @Operation(summary = "Create a task", description = "Creates a new task within the specified project.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResponseDTO.class)))
    })
    public ResponseEntity<TaskResponseDTO> createTask(@Valid @RequestBody TaskRequestDTO request) {
        TaskResponseDTO response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    @Operation(summary = "Update a task", description = "Updates the specified task's details and attachments.")
    public TaskResponseDTO updateTask(@PathVariable Long taskId, @Valid @RequestBody TaskRequestDTO request) {
        return taskService.updateTask(taskId, request);
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    @Operation(summary = "Delete a task", description = "Deletes the specified task from its project.")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reorder")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    @Operation(summary = "Reorder tasks", description = "Reorders tasks within a kanban column using their new sort order.")
    public List<TaskResponseDTO> reorder(@Valid @RequestBody TaskReorderRequestDTO request) {
        return taskService.reorderTasks(request);
    }

    @PatchMapping("/{taskId}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    @Operation(summary = "Update task status", description = "Transitions a task to a new status if allowed by the workflow.")
    public TaskResponseDTO updateStatus(@PathVariable Long taskId,
                                        @Valid @RequestBody TaskStatusUpdateRequestDTO request) {
        return taskService.changeStatus(taskId, request.status());
    }
}
