package com.sentinovo.carbuildervin.controller.auth;

import com.sentinovo.carbuildervin.controller.common.StandardApiResponse;
import com.sentinovo.carbuildervin.controller.common.BaseController;
import com.sentinovo.carbuildervin.dto.auth.UserDto;
import com.sentinovo.carbuildervin.dto.auth.UserUpdateDto;
import com.sentinovo.carbuildervin.service.user.UserService;
import com.sentinovo.carbuildervin.service.user.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "User Profile", description = "Current user profile management")
public class UserController extends BaseController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @Operation(summary = "Get current user profile", description = "Get the current authenticated user's profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User profile retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = StandardApiResponse.class)))
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<UserDto>> getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        UserDto user = userService.findByUsernameDto(username);
        log.info("Getting profile for user ID: {}", user.getId());
        
        return success(user);
    }

    @Operation(summary = "Update current user profile", description = "Update the current authenticated user's profile")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User profile updated successfully",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = StandardApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = StandardApiResponse.class))),
        @ApiResponse(responseCode = "409", description = "Email already exists",
            content = @Content(schema = @Schema(implementation = StandardApiResponse.class)))
    })
    @PatchMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<UserDto>> updateCurrentUser(
            @Valid @RequestBody UserUpdateDto updateDto,
            Authentication authentication) {
        
        String username = authentication.getName();
        UserDto currentUser = userService.findByUsernameDto(username);
        log.info("Updating profile for user ID: {}", currentUser.getId());
        UserDto updatedUser = userService.updateUser(currentUser.getId(), updateDto);
        
        log.info("User profile updated successfully for user ID: {}", updatedUser.getId());
        return success(updatedUser, "Profile updated successfully");
    }

    @Operation(summary = "Delete current user account", description = "Delete the current authenticated user's account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User account deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = StandardApiResponse.class)))
    })
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        UserDto currentUser = userService.findByUsernameDto(username);
        log.info("Deleting account for user ID: {}", currentUser.getId());
        userService.deleteUser(currentUser.getId());
        
        log.info("User account deleted successfully for user ID: {}", currentUser.getId());
        return noContent();
    }

    @Operation(summary = "Change password", description = "Change the current user's password")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully",
            content = @Content(schema = @Schema(implementation = StandardApiResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid current password",
            content = @Content(schema = @Schema(implementation = StandardApiResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = StandardApiResponse.class)))
    })
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<Object>> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        
        String username = authentication.getName();
        UserDto currentUser = userService.findByUsernameDto(username);
        log.info("Password change request for user ID: {}", currentUser.getId());
        authenticationService.changeUserPassword(currentUser.getId(), request.getCurrentPassword(), request.getNewPassword());
        
        log.info("Password changed successfully for user ID: {}", currentUser.getId());
        return success(null, "Password changed successfully");
    }

    @Schema(description = "Change password request")
    public static class ChangePasswordRequest {
        @Schema(description = "Current password", example = "currentPassword123!")
        private String currentPassword;
        
        @Schema(description = "New password", example = "newPassword123!")
        private String newPassword;

        public String getCurrentPassword() {
            return currentPassword;
        }

        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}