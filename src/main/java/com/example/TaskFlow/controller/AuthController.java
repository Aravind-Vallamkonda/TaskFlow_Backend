package com.example.TaskFlow.controller;

import com.example.TaskFlow.Constants.Constants;
import com.example.TaskFlow.Constants.ErrorCode;
import com.example.TaskFlow.dto.request.IdentifyRequestDTO;
import com.example.TaskFlow.dto.request.LoginRequestDTO;
import com.example.TaskFlow.dto.request.RegisterDTO;
import com.example.TaskFlow.dto.response.ApiMessageResponse;
import com.example.TaskFlow.dto.response.IdentifyResponseDTO;
import com.example.TaskFlow.dto.response.TokenResponse;
import com.example.TaskFlow.exception.ApiError;
import com.example.TaskFlow.model.User;
import com.example.TaskFlow.repo.UserRepository;
import com.example.TaskFlow.service.JwtService;
import com.example.TaskFlow.service.LoginFlowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Tag(name = "Authentication", description = "Operations related to user onboarding and token lifecycle management.")
@RestController
@RequestMapping("/auth")
public class AuthController {

        private static final Logger log = LoggerFactory.getLogger(AuthController.class);

        private final UserRepository userRepository;
        private final LoginFlowService loginFlowService;
        private final PasswordEncoder encoder;
        private final JwtService jwtService;

        public AuthController(UserRepository userRepository, LoginFlowService loginFlowService, JwtService jwtService,
                        PasswordEncoder encoder) {
                this.userRepository = userRepository;
                this.loginFlowService = loginFlowService;
                this.jwtService = jwtService;
                this.encoder = encoder;
        }

        @PostMapping("/register")
        @Operation(summary = "Register a new user", description = "Creates a TaskFlow account using the supplied profile and credential information.")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "User registered successfully",
                                        content = @Content(mediaType = "application/json",
                                                        schema = @Schema(implementation = ApiMessageResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid registration payload",
                                        content = @Content(mediaType = "application/json",
                                                        schema = @Schema(implementation = ApiError.class))),
                        @ApiResponse(responseCode = "409", description = "A user with the provided username or e-mail already exists",
                                        content = @Content(mediaType = "application/json",
                                                        schema = @Schema(implementation = ApiError.class)))
        })
        public ResponseEntity<ApiMessageResponse> register(@RequestBody @Valid RegisterDTO regRequest) {
                log.info("User registration started with username: {}", regRequest.username());
                String username = regRequest.username().trim();
                String email = regRequest.email().trim();
                if (userRepository.findByEmailOrUsername(email, username).isPresent()) {
                        throw new ResponseStatusException(ErrorCode.USER_ALREADY_EXISTS.getStatus(),
                                        ErrorCode.USER_ALREADY_EXISTS.getMessage());
                }
                User user = new User();
                user.setEmail(email);
                user.setUsername(username);
                user.setFirstname(regRequest.firstname().trim());
                user.setLastname(regRequest.lastname().trim());
                user.setPasswordHash(encoder.encode(regRequest.password().trim()));
                userRepository.save(user);
                log.info("User registration successful for username: {} and email: {}", user.getUsername(), user.getEmail());

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new ApiMessageResponse("User created successfully."));
        }

        @PostMapping("/identify")
        @Operation(summary = "Start login flow", description = "Validates the provided identifier and returns a short-lived flow id required for password verification.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "User identified successfully",
                                        content = @Content(mediaType = "application/json",
                                                        schema = @Schema(implementation = IdentifyResponseDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid identifier supplied",
                                        content = @Content(mediaType = "application/json",
                                                        schema = @Schema(implementation = ApiError.class))),
                        @ApiResponse(responseCode = "404", description = "No user found for the supplied identifier",
                                        content = @Content(mediaType = "application/json",
                                                        schema = @Schema(implementation = ApiError.class)))
        })
        public ResponseEntity<IdentifyResponseDTO> identify(@RequestBody @Valid IdentifyRequestDTO identifyRequest) {
                String identifier = identifyRequest.identifier().trim();
                log.info("User identification started for identifier: {}", identifier);
                Optional<User> userOptional = userRepository.findByEmailOrUsername(identifier, identifier);

                if (userOptional.isEmpty()) {
                        log.info("User identification failed for identifier: {}", identifier);
                        throw new ResponseStatusException(ErrorCode.USER_NOT_FOUND.getStatus(),
                                        ErrorCode.USER_NOT_FOUND.getMessage() + ": " + identifier);
                }

                var flow = loginFlowService.create(userOptional.get().getUsername());
                log.info("User identification successful for identifier: {}", identifier);
                return ResponseEntity.ok(new IdentifyResponseDTO(flow.id));
        }

        @PostMapping("/login")
        @Operation(summary = "Complete login", description = "Confirms the user's password and issues a new access token along with an HTTP-only refresh cookie.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Authentication successful",
                                        headers = @Header(name = HttpHeaders.SET_COOKIE,
                                                        description = "HTTP-only refresh token cookie that can be used to obtain new access tokens.",
                                                        schema = @Schema(type = "string")),
                                        content = @Content(mediaType = "application/json",
                                                        schema = @Schema(implementation = TokenResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid flow identifier supplied",
                                        content = @Content(mediaType = "application/json",
                                                        schema = @Schema(implementation = ApiError.class))),
                        @ApiResponse(responseCode = "401", description = "Password verification failed",
                                        content = @Content(mediaType = "application/json",
                                                        schema = @Schema(implementation = ApiError.class)))
        })
        public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequestDTO loginRequestDTO) {
                var flowOpt = loginFlowService.get(loginRequestDTO.flowId());
                if (flowOpt.isEmpty()) {
                        throw new ResponseStatusException(ErrorCode.INVALID_FLOWID.getStatus(),
                                        ErrorCode.INVALID_FLOWID.getMessage());
                }
                var flow = flowOpt.get();
                if (++flow.attempts > 3) {
                        throw new ResponseStatusException(ErrorCode.MAXIMUM_PASSWORD_ATTEMPTS_REACHED.getStatus(),
                                        ErrorCode.MAXIMUM_PASSWORD_ATTEMPTS_REACHED.getMessage());
                }

                var user = (flow.username != null) ? userRepository.findByUsername(flow.username).orElse(null) : null;
                if (user == null) {
                        throw new ResponseStatusException(ErrorCode.USER_NOT_FOUND.getStatus(),
                                        ErrorCode.USER_NOT_FOUND.getMessage());
                }
                log.info("User login started for username: {}", user.getUsername());
                if (!encoder.matches(loginRequestDTO.password().trim(), user.getPasswordHash())) {
                        log.info("User login failed for username: {}", user.getUsername());
                        throw new ResponseStatusException(ErrorCode.INVALID_PASSWORD.getStatus(),
                                        ErrorCode.INVALID_PASSWORD.getMessage());
                }

                List<String> roles = List.of("USER");
                String access = jwtService.createAccessToken(user.getUsername(), roles);
                String refresh = jwtService.createRefreshToken(user.getUsername(), roles);
                long refreshMaxAge = Duration.ofMinutes(jwtService.getRefreshTokenValidity()).getSeconds();
                ResponseCookie responseCookie = ResponseCookie.from(Constants.REFRESH_TOKEN_COOKIE, refresh)
                                .httpOnly(true)
                                .secure(false)
                                .path("")
                                .maxAge(refreshMaxAge)
                                .build();
                log.info("User login successful for username: {}", user.getUsername());
                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                                .body(new TokenResponse(access));
        }

        @PostMapping("/refresh")
        @Operation(summary = "Refresh access token", description = "Exchanges a valid refresh token cookie for a new access token.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Access token refreshed",
                                        content = @Content(mediaType = "application/json",
                                                        schema = @Schema(implementation = TokenResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Refresh token missing or malformed",
                                        content = @Content(mediaType = "application/json",
                                                        schema = @Schema(implementation = ApiError.class))),
                        @ApiResponse(responseCode = "401", description = "Refresh token expired or invalid",
                                        content = @Content(mediaType = "application/json",
                                                        schema = @Schema(implementation = ApiError.class)))
        })
        public ResponseEntity<TokenResponse> refresh(@Parameter(hidden = true) HttpServletRequest request) {
                Cookie[] cookies = request.getCookies();
                if (cookies == null || cookies.length == 0) {
                        log.info("Refresh token cookie missing from request.");
                        throw new ResponseStatusException(ErrorCode.BAD_REQUEST.getStatus(),
                                        ErrorCode.BAD_REQUEST.getMessage());
                }

                String refreshToken = Arrays.stream(cookies)
                                .filter(cookie -> Constants.REFRESH_TOKEN_COOKIE.equals(cookie.getName()))
                                .map(Cookie::getValue)
                                .filter(token -> token != null && !token.isEmpty())
                                .findFirst()
                                .orElse(null);

                if (refreshToken == null) {
                        log.info("Refresh token cookie missing from request.");
                        throw new ResponseStatusException(ErrorCode.INVALID_REFRESH_TOKEN.getStatus(),
                                        ErrorCode.INVALID_REFRESH_TOKEN.getMessage());
                }

                var claims = jwtService.parse(refreshToken);
                if (jwtService.isTokenExpired(claims) || !jwtService.isRefreshToken(claims)) {
                        log.info("Refresh token cookie has expired or is invalid.");
                        throw new ResponseStatusException(ErrorCode.INVALID_REFRESH_TOKEN.getStatus(),
                                        ErrorCode.INVALID_REFRESH_TOKEN.getMessage());
                }

                String username = jwtService.extractUsername(claims);
                if (username == null || username.isBlank()) {
                        log.info("Refresh token cookie missing username from request.");
                        throw new ResponseStatusException(ErrorCode.INVALID_REFRESH_TOKEN.getStatus(),
                                        ErrorCode.INVALID_REFRESH_TOKEN.getMessage());
                }

                List<String> roles = jwtService.roles(claims);
                if (roles == null || roles.isEmpty()) {
                        roles = List.of("USER");
                }

                String access = jwtService.createAccessToken(username, roles);
                log.info("Access token refreshed for username {}", username);
                return ResponseEntity.ok(new TokenResponse(access));
        }
}
