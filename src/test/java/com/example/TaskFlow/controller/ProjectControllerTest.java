package com.example.TaskFlow.controller;

import com.example.TaskFlow.config.SecurityConfig;
import com.example.TaskFlow.dto.request.ProjectRequestDTO;
import com.example.TaskFlow.dto.response.ProjectResponseDTO;
import com.example.TaskFlow.jwt.JwtAuthFilter;
import com.example.TaskFlow.service.ProjectService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProjectController.class)
@Import(SecurityConfig.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

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
    void listProjectsRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/projects").param("teamId", "5"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void listProjectsReturnsTeamProjects() throws Exception {
        ProjectResponseDTO response = ProjectResponseDTO.builder()
                .id(1L)
                .teamId(7L)
                .name("Alpha")
                .build();
        given(projectService.listProjects(7L, "user")).willReturn(List.of(response));

        mockMvc.perform(get("/api/projects").param("teamId", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Alpha"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProjectReturnsCreatedProject() throws Exception {
        ProjectResponseDTO response = ProjectResponseDTO.builder()
                .id(20L)
                .teamId(5L)
                .name("New Project")
                .description("Build integrations")
                .startDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(3))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        given(projectService.createProject(any(ProjectRequestDTO.class), eq("user"))).willReturn(response);

        ProjectRequestDTO request = new ProjectRequestDTO();
        request.setTeamId(5L);
        request.setName("New Project");
        request.setDescription("Build integrations");
        request.setStartDate(LocalDate.now());
        request.setDueDate(LocalDate.now().plusDays(3));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20L))
                .andExpect(jsonPath("$.name").value("New Project"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProjectReturnsNoContent() throws Exception {
        Mockito.doNothing().when(projectService).deleteProject(9L, "user");

        mockMvc.perform(delete("/api/projects/9"))
                .andExpect(status().isNoContent());
    }
}
