package com.example.TaskFlow.controller;

import com.example.TaskFlow.Constants.ErrorCode;
import com.example.TaskFlow.dto.request.ChangePasswordDTO;
import com.example.TaskFlow.dto.response.ApiMessageResponse;
import com.example.TaskFlow.dto.response.UserProfileResponse;
import com.example.TaskFlow.exception.ApiError;
import com.example.TaskFlow.model.User;
import com.example.TaskFlow.repo.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Tag(name = "Users", description = "Operations available to authenticated TaskFlow users.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/user")
public class UserController {

        private static final Logger log = LoggerFactory.getLogger(UserController.class);

        private final UserRepository userRepository;
        private final PasswordEncoder encoder;

        public UserController(UserRepository userRepository, PasswordEncoder encoder) {
                this.userRepository = userRepository;
                this.encoder = encoder;
        }

        @GetMapping("/me")
        @Operation(summary = "Retrieve the authenticated user's profile", description = "Returns the profile information for the currently signed-in user.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                                        content = @Content(mediaType = "application/json",
                                                        schema = @Schema(implementation = UserProfileResponse.class))),
                        @ApiResponse(responseCode = "400", description = "No authenticated user found in context",
                                        content = @Content(mediaType = "application/json",
                                                        schema = @Schema(implementation = ApiError.class))),
                        @ApiResponse(responseCode = "401", description = "Authentication required",
                                        content = @Content(mediaType = "application/json",
                                                        schema = @Schema(implementation = ApiError.class)))
        })
        public ResponseEntity<UserProfileResponse> getMe() {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth == null || !auth.isAuthenticated()) {
                        throw new ResponseStatusException(ErrorCode.BAD_REQUEST.getStatus(),
                                        ErrorCode.BAD_REQUEST.getMessage());
                }
                String username = auth.getName();
                Optional<User> user = userRepository.findByUsername(username);
                if (user.isEmpty()) {
                        throw new ResponseStatusException(ErrorCode.USER_NOT_FOUND.getStatus(),
                                        ErrorCode.USER_NOT_FOUND.getMessage());
                }
                return ResponseEntity.ok(UserProfileResponse.from(user.get()));
        }

        @PostMapping("/updatePassword")
        @Operation(summary = "Update password", description = "Allows the authenticated user to change their password after providing the current password.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Password updated successfully",
                                        content = @Content(mediaType = "application/json",
                                                        schema = @Schema(implementation = ApiMessageResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Validation failed or old password is incorrect",
                                        content = @Content(mediaType = "application/json",
                                                        schema = @Schema(implementation = ApiError.class))),
                        @ApiResponse(responseCode = "401", description = "Authentication required",
                                        content = @Content(mediaType = "application/json",
                                                        schema = @Schema(implementation = ApiError.class)))
        })
        public ResponseEntity<ApiMessageResponse> updatePassword(@RequestBody @Valid ChangePasswordDTO req) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth == null || !auth.isAuthenticated()) {
                        throw new ResponseStatusException(ErrorCode.BAD_REQUEST.getStatus(),
                                        ErrorCode.BAD_REQUEST.getMessage());
                }
                String username = auth.getName();
                Optional<User> userOpt = userRepository.findByUsername(username);
                if (userOpt.isEmpty()) {
                        throw new ResponseStatusException(ErrorCode.USER_NOT_FOUND.getStatus(),
                                        ErrorCode.USER_NOT_FOUND.getMessage());
                }
                User user = userOpt.get();
                if (!encoder.matches(req.oldPassword().trim(), user.getPasswordHash())) {
                        log.info("User change password request failed for username: {}", user.getUsername());
                        throw new ResponseStatusException(ErrorCode.INVALID_PASSWORD.getStatus(),
                                        ErrorCode.INVALID_PASSWORD.getMessage());
                }
                user.setPasswordHash(encoder.encode(req.newPassword().trim()));
                userRepository.save(user);
                log.info("Password successfully updated for username: {}", user.getUsername());
                return ResponseEntity.ok(new ApiMessageResponse("Password updated successfully."));
        }
}
