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

import java.time.OffsetDateTime;
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

    // ========================================
    // MCP API Key Management
    // ========================================

    @Operation(summary = "Get MCP API key status", description = "Get the current MCP API key status (masked) and creation date")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "API key status retrieved successfully",
            content = @Content(schema = @Schema(implementation = McpApiKeyResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = StandardApiResponse.class)))
    })
    @GetMapping("/mcp-api-key")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<McpApiKeyResponse>> getMcpApiKeyStatus(Authentication authentication) {
        String username = authentication.getName();
        UserDto currentUser = userService.findByUsernameDto(username);
        log.info("Getting MCP API key status for user ID: {}", currentUser.getId());

        String maskedKey = userService.getMaskedMcpApiKey(currentUser.getId());
        OffsetDateTime createdAt = userService.getMcpApiKeyCreatedAt(currentUser.getId());
        boolean hasKey = maskedKey != null;

        McpApiKeyResponse response = new McpApiKeyResponse(hasKey, maskedKey, createdAt);
        return success(response);
    }

    @Operation(summary = "Generate MCP API key", description = "Generate a new MCP API key (revokes existing key)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "API key generated successfully",
            content = @Content(schema = @Schema(implementation = McpApiKeyGenerateResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = StandardApiResponse.class)))
    })
    @PostMapping("/mcp-api-key")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StandardApiResponse<McpApiKeyGenerateResponse>> generateMcpApiKey(Authentication authentication) {
        String username = authentication.getName();
        UserDto currentUser = userService.findByUsernameDto(username);
        log.info("Generating MCP API key for user ID: {}", currentUser.getId());

        String apiKey = userService.generateMcpApiKey(currentUser.getId());

        log.info("MCP API key generated successfully for user ID: {}", currentUser.getId());
        McpApiKeyGenerateResponse response = new McpApiKeyGenerateResponse(apiKey);
        return success(response, "MCP API key generated successfully. Save this key - it won't be shown again!");
    }

    @Operation(summary = "Revoke MCP API key", description = "Revoke the current MCP API key")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "API key revoked successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = StandardApiResponse.class)))
    })
    @DeleteMapping("/mcp-api-key")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> revokeMcpApiKey(Authentication authentication) {
        String username = authentication.getName();
        UserDto currentUser = userService.findByUsernameDto(username);
        log.info("Revoking MCP API key for user ID: {}", currentUser.getId());

        userService.revokeMcpApiKey(currentUser.getId());

        log.info("MCP API key revoked successfully for user ID: {}", currentUser.getId());
        return noContent();
    }

    @Schema(description = "MCP API key status response")
    public static class McpApiKeyResponse {
        @Schema(description = "Whether user has an API key", example = "true")
        private boolean hasApiKey;

        @Schema(description = "Masked API key (first 8 + last 4 chars)", example = "a1b2c3d4...ef56")
        private String maskedKey;

        @Schema(description = "When the API key was created")
        private OffsetDateTime createdAt;

        public McpApiKeyResponse(boolean hasApiKey, String maskedKey, OffsetDateTime createdAt) {
            this.hasApiKey = hasApiKey;
            this.maskedKey = maskedKey;
            this.createdAt = createdAt;
        }

        public boolean isHasApiKey() { return hasApiKey; }
        public void setHasApiKey(boolean hasApiKey) { this.hasApiKey = hasApiKey; }
        public String getMaskedKey() { return maskedKey; }
        public void setMaskedKey(String maskedKey) { this.maskedKey = maskedKey; }
        public OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    }

    @Schema(description = "MCP API key generate response")
    public static class McpApiKeyGenerateResponse {
        @Schema(description = "The generated API key (shown only once)", example = "a1b2c3d4e5f6...")
        private String apiKey;

        public McpApiKeyGenerateResponse(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
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