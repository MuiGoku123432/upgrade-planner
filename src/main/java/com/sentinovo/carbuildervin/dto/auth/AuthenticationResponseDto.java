package com.sentinovo.carbuildervin.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after successful authentication")
public class AuthenticationResponseDto {

    @Schema(description = "Authenticated user information")
    private UserDto user;

    @Schema(description = "JWT token for API access")
    private String token;

    @Schema(description = "Token type", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Token expiration time in seconds", example = "3600")
    private Long expiresIn;

    @Schema(description = "Success message", example = "Authentication successful")
    private String message;
}