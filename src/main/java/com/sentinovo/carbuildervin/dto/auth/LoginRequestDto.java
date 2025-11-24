package com.sentinovo.carbuildervin.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for user login")
public class LoginRequestDto {

    @NotBlank(message = "Username or email is required")
    @Schema(description = "Username or email address", example = "john_doe")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    @Schema(description = "User password", example = "SecurePassword123!")
    private String password;
}