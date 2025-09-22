package com.example.TaskFlow.dto.response;

import com.example.TaskFlow.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Details about the authenticated TaskFlow user.")
public record UserProfileResponse(
                @Schema(description = "Unique identifier of the user.", example = "42") Long id,
                @Schema(description = "Username used to authenticate.", example = "alex.morgan") String username,
                @Schema(description = "Primary e-mail address of the user.", example = "alex.morgan@example.com") String email,
                @Schema(description = "First name as stored in the profile.", example = "Alex") String firstname,
                @Schema(description = "Last name as stored in the profile.", example = "Morgan") String lastname,
                @Schema(description = "Optional middle name if provided.", example = "Taylor") String middlename,
                @Schema(description = "Whether the account is active and can sign in.", example = "true") boolean active,
                @Schema(description = "Whether the account is locked due to administrative action.", example = "false") boolean locked,
                @Schema(description = "Whether the account is soft deleted.", example = "false") boolean deleted,
                @Schema(description = "Number of consecutive failed login attempts.", example = "0") Integer failedLoginAttempts
) {
        public static UserProfileResponse from(User user) {
                return new UserProfileResponse(
                                user.getId(),
                                user.getUsername(),
                                user.getEmail(),
                                user.getFirstname(),
                                user.getLastname(),
                                user.getMiddlename(),
                                Boolean.TRUE.equals(user.getIsActive()),
                                Boolean.TRUE.equals(user.getIsLocked()),
                                Boolean.TRUE.equals(user.getIsDeleted()),
                                user.getFailedLoginAttempts()
                );
        }
}
