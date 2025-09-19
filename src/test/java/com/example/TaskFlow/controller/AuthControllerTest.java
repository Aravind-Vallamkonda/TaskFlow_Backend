package com.example.TaskFlow.controller;


import com.example.TaskFlow.Constants.Constants;
import com.example.TaskFlow.Constants.ErrorCode;
import com.example.TaskFlow.dto.request.IdentifyRequestDTO;
import com.example.TaskFlow.dto.request.LoginRequestDTO;
import com.example.TaskFlow.dto.request.RegisterDTO;
import com.example.TaskFlow.exception.GlobalExceptionHandler;
import com.example.TaskFlow.model.User;
import com.example.TaskFlow.repo.UserRepository;
import com.example.TaskFlow.service.JwtService;
import com.example.TaskFlow.service.LoginFlowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import jakarta.servlet.http.Cookie;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMVC;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private LoginFlowService loginFlowService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private PasswordEncoder passwordEncoder;


    @Test
    void registerShouldPersistNewUser() throws Exception{
        RegisterDTO request = new RegisterDTO("alice","alice@example.com","Alice@123!","Alice","Smith");

        when(userRepository.findByEmailOrUsername("alice@example.com","alice")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Alice@123!")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMVC.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User Created"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getUsername()).isEqualTo("alice");
        assertThat(saved.getEmail()).isEqualTo("alice@example.com");
        assertThat(saved.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(saved.getFirstname()).isEqualTo("Alice");
        assertThat(saved.getLastname()).isEqualTo("Smith");
    }

    @Test
    void registerShouldReturnConflictWhenUserExists() throws Exception{
        RegisterDTO request = new RegisterDTO("alice","alice@example.com","Alice@123!","Alice","Smith");
        when(userRepository.findByEmailOrUsername("alice@example.com","alice")).thenReturn(Optional.of(new User()));

        mockMVC.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ErrorCode.USER_ALREADY_EXISTS.getMessage()));
    }

    @Test
    void refreshShouldFailWhenCookieIsMissing() throws Exception{
        mockMVC.perform(post("/auth/refresh"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"));
    }

    @Test
    void identifyShouldReturnFlowidForExistingUser() throws Exception{
        IdentifyRequestDTO identify = new IdentifyRequestDTO("alice");
        User user = new User();
        user.setUsername("alice");
        when(userRepository.findByEmailOrUsername("alice", "alice")).thenReturn(Optional.of(user));
        LoginFlowService.Flow flow = new LoginFlowService().create("alice");
        when(loginFlowService.create("alice")).thenReturn(flow);

        mockMVC.perform(post("/auth/identify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(identify)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flowId").value(flow.id));
    }

    @Test
    void identifyShouldReturnNotFoundWhenUserMissing() throws Exception {
        IdentifyRequestDTO request = new IdentifyRequestDTO("void");
        when(userRepository.findByEmailOrUsername("void", "void")).thenReturn(Optional.empty());

        mockMVC.perform(post("/auth/identify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found.: void"));
    }

    @Test
    void loginShouldReturnTokensWhenCredentialsValid() throws Exception {
        LoginFlowService.Flow flow = new LoginFlowService().create("alice");
        LoginRequestDTO request = new LoginRequestDTO(" Password123", flow.id);
        User user = new User();
        user.setUsername("alice");
        user.setPasswordHash("stored-hash");

        when(loginFlowService.get(flow.id)).thenReturn(Optional.of(flow));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", "stored-hash")).thenReturn(true);
        when(jwtService.createAccessToken(eq("alice"), anyList())).thenReturn("access-token-value");
        when(jwtService.createRefreshToken(eq("alice"), anyList())).thenReturn("refresh-token-value");
        when(jwtService.getRefreshTokenValidity()).thenReturn(120L);

        mockMVC.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access-token").value("access-token-value"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=refresh-token-value")));
    }

    @Test
    void loginShouldReturnBadRequestWhenFlowMissing() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("Password123", "missing-flow");
        when(loginFlowService.get("missing-flow")).thenReturn(Optional.empty());

        mockMVC.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid FlowId"));
    }

    @Test
    void loginShouldReturnBadRequestWhenAttemptsExceeded() throws Exception {
        LoginFlowService.Flow flow = new LoginFlowService().create("alice");
        flow.attempts = 3;
        LoginRequestDTO request = new LoginRequestDTO("Password123", flow.id);
        when(loginFlowService.get(flow.id)).thenReturn(Optional.of(flow));

        mockMVC.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Maximum Password Attempts Reached."));
    }

    @Test
    void loginShouldReturnUnauthorizedWhenPasswordInvalid() throws Exception {
        LoginFlowService.Flow flow = new LoginFlowService().create("alice");
        LoginRequestDTO request = new LoginRequestDTO("Password123", flow.id);
        User user = new User();
        user.setUsername("alice");
        user.setPasswordHash("stored-hash");

        when(loginFlowService.get(flow.id)).thenReturn(Optional.of(flow));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", "stored-hash")).thenReturn(false);

        mockMVC.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid Password"));
    }
    @Test
    void refreshShouldReturnNewAccessTokenWhenCookieValid() throws Exception {
        Cookie refreshCookie = new Cookie(Constants.REFRESH_TOKEN_COOKIE, "valid-refresh-token");
        Claims claims = Jwts.claims().build();

        when(jwtService.parse("valid-refresh-token")).thenReturn(claims);
        when(jwtService.isTokenExpired(claims)).thenReturn(false);
        when(jwtService.isRefreshToken(claims)).thenReturn(true);
        when(jwtService.extractUsername(claims)).thenReturn("alice");
        when(jwtService.roles(claims)).thenReturn(List.of("USER"));
        when(jwtService.createAccessToken(eq("alice"), anyList())).thenReturn("new-access-token");

        mockMVC.perform(post("/auth/refresh").cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access-token").value("new-access-token"));
    }

    @Test
    void refreshShouldFailWhenCookieMissing() throws Exception {
        mockMVC.perform(post("/auth/refresh"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Bad Request"));
    }

    @Test
    void refreshShouldFailWhenTokenExpiredOrInvalid() throws Exception {
        Cookie refreshCookie = new Cookie(Constants.REFRESH_TOKEN_COOKIE, "expired");
        Claims claims = Jwts.claims().build();

        when(jwtService.parse("expired")).thenReturn(claims);
        when(jwtService.isTokenExpired(claims)).thenReturn(true);

        mockMVC.perform(post("/auth/refresh").cookie(refreshCookie))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Refresh Token is Invalid"));
    }
}
