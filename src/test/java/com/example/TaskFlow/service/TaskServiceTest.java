package com.example.TaskFlow.service;

import com.example.TaskFlow.dto.mapper.TaskMapper;
import com.example.TaskFlow.dto.request.AttachmentPayloadDTO;
import com.example.TaskFlow.dto.request.TaskReorderRequestDTO;
import com.example.TaskFlow.dto.request.TaskRequestDTO;
import com.example.TaskFlow.dto.response.TaskResponseDTO;
import com.example.TaskFlow.model.Project;
import com.example.TaskFlow.model.Task;
import com.example.TaskFlow.model.User;
import com.example.TaskFlow.model.enums.TaskPriority;
import com.example.TaskFlow.model.enums.TaskStatus;
import com.example.TaskFlow.repo.ProjectRepository;
import com.example.TaskFlow.repo.TaskRepository;
import com.example.TaskFlow.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, projectRepository, userRepository, new TaskMapper());
    }

    @Test
    void createTaskShouldPersistAttachmentsAndAssignment() {
        TaskRequestDTO request = buildRequest();
        Project project = new Project();
        project.setId(1L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        User assignee = new User();
        assignee.setId(3L);
        assignee.setUsername("assignee");
        when(userRepository.findById(3L)).thenReturn(Optional.of(assignee));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(10L);
            task.getAttachments().forEach(att -> {
                att.setId((long) (task.getAttachments().indexOf(att) + 1));
                att.setTask(task);
            });
            return task;
        });

        TaskResponseDTO response = taskService.createTask(request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getAssignment().assigneeId()).isEqualTo(3L);
        assertThat(response.getAttachments()).hasSize(1);
        assertThat(response.getAttachments().get(0).getFileName()).isEqualTo("design.pdf");
    }

    @Test
    void changeStatusShouldValidateTransitions() {
        Task task = new Task();
        task.setId(5L);
        task.setStatus(TaskStatus.TODO);
        when(taskRepository.findById(5L)).thenReturn(Optional.of(task));

        TaskResponseDTO response = taskService.changeStatus(5L, TaskStatus.IN_PROGRESS);
        assertThat(response.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(5L)).thenReturn(Optional.of(task));
        assertThatThrownBy(() -> taskService.changeStatus(5L, TaskStatus.BACKLOG))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Illegal status transition");
    }

    @Test
    void reorderTasksShouldApplyNewOrder() {
        TaskReorderRequestDTO request = new TaskReorderRequestDTO();
        request.setProjectId(7L);
        request.setStatus(TaskStatus.TODO);
        TaskReorderRequestDTO.PositionDTO first = new TaskReorderRequestDTO.PositionDTO();
        first.setTaskId(1L);
        first.setSortOrder(2);
        TaskReorderRequestDTO.PositionDTO second = new TaskReorderRequestDTO.PositionDTO();
        second.setTaskId(2L);
        second.setSortOrder(1);
        request.setPositions(List.of(first, second));

        Task taskA = new Task();
        taskA.setId(1L);
        taskA.setStatus(TaskStatus.TODO);
        Project project = new Project();
        project.setId(7L);
        taskA.setProject(project);
        Task taskB = new Task();
        taskB.setId(2L);
        taskB.setStatus(TaskStatus.TODO);
        taskB.setProject(project);
        when(taskRepository.findAllById(Mockito.<Iterable<Long>>any())).thenReturn(List.of(taskA, taskB));

        List<TaskResponseDTO> responses = taskService.reorderTasks(request);
        assertThat(responses).extracting(TaskResponseDTO::getId).containsExactly(2L, 1L);
    }

    private TaskRequestDTO buildRequest() {
        TaskRequestDTO request = new TaskRequestDTO();
        request.setProjectId(1L);
        request.setTitle("Create mocks");
        request.setDescription("Write documentation");
        request.setPriority(TaskPriority.HIGH);
        request.setAssigneeId(3L);
        request.setDueDate(LocalDate.now().plusDays(5));
        request.setAssignedAt(Instant.now());
        AttachmentPayloadDTO attachment = new AttachmentPayloadDTO();
        attachment.setFileName("design.pdf");
        attachment.setStorageUrl("https://files/design.pdf");
        attachment.setContentType("application/pdf");
        attachment.setFileSize(1024L);
        request.setAttachments(List.of(attachment));
        return request;
    }
}
