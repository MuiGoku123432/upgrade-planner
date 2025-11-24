package com.sentinovo.carbuildervin.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Extended user profile with roles")
public class UserProfileDto {

    @Schema(description = "User ID", example = "f2d9b6c3-8713-4fca-b33e-9c38e8d897aa")
    private UUID id;

    @Schema(description = "Username", example = "john_doe")
    private String username;

    @Schema(description = "Email address", example = "john@example.com")
    private String email;

    @Schema(description = "Display name", example = "John Doe")
    private String displayName;

    @Schema(description = "User roles")
    private List<String> roles;

    @Schema(description = "Account status", example = "true")
    private Boolean isActive;
    
    @Schema(description = "Number of vehicles owned", example = "3")
    private Long vehicleCount;

    @Schema(description = "Account creation timestamp", example = "2025-11-22T15:30:00Z")
    private OffsetDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2025-11-22T15:30:00Z")
    private OffsetDateTime updatedAt;

    @Schema(description = "Last login timestamp", example = "2025-11-22T15:30:00Z")
    private OffsetDateTime lastLoginAt;
}