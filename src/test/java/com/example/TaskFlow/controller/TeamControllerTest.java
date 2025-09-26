package com.example.TaskFlow.controller;

import com.example.TaskFlow.config.SecurityConfig;
import com.example.TaskFlow.dto.request.TeamCreateRequestDTO;
import com.example.TaskFlow.dto.request.TeamInviteRequestDTO;
import com.example.TaskFlow.dto.response.TeamMembershipResponseDTO;
import com.example.TaskFlow.dto.response.TeamResponseDTO;
import com.example.TaskFlow.jwt.JwtAuthFilter;
import com.example.TaskFlow.model.enums.MembershipStatus;
import com.example.TaskFlow.model.enums.TeamRole;
import com.example.TaskFlow.service.TeamService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TeamController.class)
@Import(SecurityConfig.class)
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TeamService teamService;

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
    void routesRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user", roles = "MEMBER")
    void createTeamReturnsCreated() throws Exception {
        TeamResponseDTO response = TeamResponseDTO.builder()
                .id(22L)
                .name("Core")
                .member(TeamMembershipResponseDTO.builder()
                        .id(5L)
                        .userId(1L)
                        .username("owner")
                        .role(TeamRole.ADMIN)
                        .status(MembershipStatus.ACTIVE)
                        .invitedAt(Instant.now())
                        .joinedAt(Instant.now())
                        .build())
                .build();
        given(teamService.createTeam(any(TeamCreateRequestDTO.class), eq("user"))).willReturn(response);

        TeamCreateRequestDTO request = new TeamCreateRequestDTO();
        request.setName("Core");

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Core"))
                .andExpect(jsonPath("$.members[0].role").value("ADMIN"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void inviteMemberReturnsUpdatedTeam() throws Exception {
        TeamResponseDTO response = TeamResponseDTO.builder()
                .id(30L)
                .name("Platform")
                .member(TeamMembershipResponseDTO.builder()
                        .id(11L)
                        .userId(5L)
                        .username("invitee")
                        .role(TeamRole.MEMBER)
                        .status(MembershipStatus.INVITED)
                        .invitedAt(Instant.now())
                        .build())
                .build();
        given(teamService.inviteMember(eq(30L), any(TeamInviteRequestDTO.class), eq("admin"))).willReturn(response);

        TeamInviteRequestDTO request = new TeamInviteRequestDTO();
        request.setUserId(5L);

        mockMvc.perform(post("/api/teams/30/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members[0].status").value("INVITED"));
    }
}
