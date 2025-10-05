package com.example.TaskFlow.controller;

import com.example.TaskFlow.config.SecurityConfig;
import com.example.TaskFlow.dto.request.TaskRequestDTO;
import com.example.TaskFlow.dto.request.TaskStatusUpdateRequestDTO;
import com.example.TaskFlow.dto.response.TaskAssignmentDTO;
import com.example.TaskFlow.dto.response.TaskResponseDTO;
import com.example.TaskFlow.jwt.JwtAuthFilter;
import com.example.TaskFlow.model.enums.TaskPriority;
import com.example.TaskFlow.model.enums.TaskStatus;
import com.example.TaskFlow.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TaskController.class)
@Import(SecurityConfig.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() throws Exception {
        Mockito.doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((jakarta.servlet.FilterChain) args[2]).doFilter(args[0], args[1]);
            return null;
        }).when(jwtAuthFilter).doFilter(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void listTasksRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void createTaskReturnsCreatedTask() throws Exception {
        TaskResponseDTO response = TaskResponseDTO.builder()
                .id(100L)
                .title("Plan sprint")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .assignment(new TaskAssignmentDTO(1L, null, Instant.now(), null, null))
                .attachments(List.of())
                .comments(List.of())
                .build();
        given(taskService.createTask(any(TaskRequestDTO.class))).willReturn(response);

        TaskRequestDTO request = new TaskRequestDTO();
        request.setProjectId(9L);
        request.setTitle("Plan sprint");
        request.setStatus(TaskStatus.TODO);
        request.setPriority(TaskPriority.MEDIUM);
        request.setDueDate(LocalDate.now().plusDays(2));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.title").value("Plan sprint"));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void updateStatusDelegatesToService() throws Exception {
        TaskResponseDTO response = TaskResponseDTO.builder()
                .id(5L)
                .status(TaskStatus.IN_PROGRESS)
                .priority(TaskPriority.HIGH)
                .assignment(new TaskAssignmentDTO(1L, null, Instant.now(), null, null))
                .attachments(List.of())
                .comments(List.of())
                .build();
        given(taskService.changeStatus(eq(5L), eq(TaskStatus.IN_PROGRESS))).willReturn(response);

        mockMvc.perform(patch("/api/tasks/5/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskStatusUpdateRequestDTO(TaskStatus.IN_PROGRESS))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }
}
