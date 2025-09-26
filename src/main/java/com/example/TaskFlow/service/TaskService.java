package com.example.TaskFlow.service;

import com.example.TaskFlow.dto.mapper.TaskMapper;
import com.example.TaskFlow.dto.request.TaskReorderRequestDTO;
import com.example.TaskFlow.dto.request.TaskRequestDTO;
import com.example.TaskFlow.dto.response.TaskResponseDTO;
import com.example.TaskFlow.model.Project;
import com.example.TaskFlow.model.Task;
import com.example.TaskFlow.model.User;
import com.example.TaskFlow.model.enums.TaskStatus;
import com.example.TaskFlow.repo.ProjectRepository;
import com.example.TaskFlow.repo.TaskRepository;
import com.example.TaskFlow.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       UserRepository userRepository,
                       TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskMapper = taskMapper;
    }

    @Transactional
    public TaskResponseDTO createTask(TaskRequestDTO request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));
        Task task = new Task();
        task.setProject(project);
        applyUpsert(request, task);
        Task saved = taskRepository.save(task);
        return taskMapper.toResponse(saved);
    }

    @Transactional
    public TaskResponseDTO updateTask(Long taskId, TaskRequestDTO request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        if (!Objects.equals(task.getProject().getId(), request.getProjectId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot move task between projects");
        }
        applyUpsert(request, task);
        return taskMapper.toResponse(task);
    }

    @Transactional
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        taskRepository.delete(task);
    }

    @Transactional
    public TaskResponseDTO changeStatus(Long taskId, TaskStatus status) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        if (task.getStatus() == status) {
            return taskMapper.toResponse(task);
        }
        if (!task.getStatus().canTransitionTo(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Illegal status transition");
        }
        task.setStatus(status);
        return taskMapper.toResponse(task);
    }

    @Transactional
    public List<TaskResponseDTO> reorderTasks(TaskReorderRequestDTO request) {
        Map<Long, Integer> desiredOrder = request.getPositions().stream()
                .collect(Collectors.toMap(TaskReorderRequestDTO.PositionDTO::getTaskId, TaskReorderRequestDTO.PositionDTO::getSortOrder));
        List<Task> tasks = taskRepository.findAllById(desiredOrder.keySet());
        if (tasks.size() != desiredOrder.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more tasks were not found");
        }
        for (Task task : tasks) {
            if (!Objects.equals(task.getProject().getId(), request.getProjectId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All tasks must belong to the same project");
            }
            if (task.getStatus() != request.getStatus()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tasks must be in the target column to reorder");
            }
            task.setSortOrder(desiredOrder.get(task.getId()));
        }
        tasks.sort(Comparator.comparing(Task::getSortOrder));
        taskRepository.saveAll(tasks);
        return tasks.stream().map(taskMapper::toResponse).toList();
    }

    @Transactional
    public List<TaskResponseDTO> getTasks(Long projectId, Optional<TaskStatus> status, Optional<Long> assigneeId) {
        List<Task> tasks;
        if (projectId != null) {
            tasks = taskRepository.findByProjectIdOrderBySortOrderAsc(projectId);
        } else {
            tasks = taskRepository.findAll();
            tasks.sort(Comparator
                    .comparing((Task task) -> task.getProject() != null ? task.getProject().getId() : Long.MAX_VALUE)
                    .thenComparing(task -> Optional.ofNullable(task.getSortOrder()).orElse(Integer.MAX_VALUE)));
        }
        return tasks.stream()
                .filter(task -> status.map(task.getStatus()::equals).orElse(true))
                .filter(task -> assigneeId.map(id -> {
                    if (task.getAssignment() == null || task.getAssignment().getAssignee() == null) {
                        return false;
                    }
                    return id.equals(task.getAssignment().getAssignee().getId());
                }).orElse(true))
                .map(taskMapper::toResponse)
                .toList();
    }

    private void applyUpsert(TaskRequestDTO request, Task task) {
        User assignee = request.getAssigneeId() != null
                ? userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignee not found"))
                : null;
        User delegate = request.getDelegateId() != null
                ? userRepository.findById(request.getDelegateId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delegate not found"))
                : null;
        taskMapper.applyUpsert(request, task, assignee, delegate);
        taskMapper.mergeAttachments(task, request.getAttachments(), request.getRemoveAttachmentIds());
    }
}
