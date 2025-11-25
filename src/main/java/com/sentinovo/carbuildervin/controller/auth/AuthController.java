package com.sentinovo.carbuildervin.controller.auth;

import com.sentinovo.carbuildervin.controller.common.StandardApiResponse;
import com.sentinovo.carbuildervin.controller.common.BaseController;
import com.sentinovo.carbuildervin.dto.auth.LoginRequestDto;
import com.sentinovo.carbuildervin.dto.auth.AuthenticationResponseDto;
import com.sentinovo.carbuildervin.dto.auth.RegisterRequestDto;
import com.sentinovo.carbuildervin.dto.auth.UserDto;
import com.sentinovo.carbuildervin.service.user.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration")
public class AuthController extends BaseController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Register new user", description = "Create a new user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = StandardApiResponse.class))),
        @ApiResponse(responseCode = "409", description = "Username or email already exists",
            content = @Content(schema = @Schema(implementation = StandardApiResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<StandardApiResponse<UserDto>> register(@Valid @RequestBody RegisterRequestDto registrationDto) {
        log.info("User registration attempt for username: {}", registrationDto.getUsername());
        
        UserDto user = authenticationService.registerUser(registrationDto);
        
        log.info("User registered successfully with ID: {}", user.getId());
        return created(user, "User registered successfully");
    }

    @Operation(summary = "User login", description = "Authenticate user and return access token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authentication successful",
            content = @Content(schema = @Schema(implementation = AuthenticationResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials",
            content = @Content(schema = @Schema(implementation = StandardApiResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<StandardApiResponse<AuthenticationResponseDto>> login(@Valid @RequestBody LoginRequestDto authRequest) {
        log.info("Authentication attempt for user: {}", authRequest.getUsernameOrEmail());
        
        AuthenticationResponseDto response = authenticationService.authenticateUser(authRequest);
        
        log.info("User authenticated successfully");
        return success(response, "Authentication successful");
    }

    @Operation(summary = "Logout user", description = "Invalidate user session")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful",
            content = @Content(schema = @Schema(implementation = StandardApiResponse.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<StandardApiResponse<Object>> logout() {
        log.info("User logout");
        
        authenticationService.logout();
        
        return success(null, "Logout successful");
    }

    @Operation(summary = "Refresh token", description = "Refresh access token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
            content = @Content(schema = @Schema(implementation = AuthenticationResponseDto.class))),
        @ApiResponse(responseCode = "401", description = "Invalid or expired token",
            content = @Content(schema = @Schema(implementation = StandardApiResponse.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<StandardApiResponse<AuthenticationResponseDto>> refreshToken(@RequestHeader("Authorization") String token) {
        log.info("Token refresh attempt");
        
        String accessToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        AuthenticationResponseDto response = authenticationService.refreshToken(accessToken);
        
        log.info("Token refreshed successfully");
        return success(response, "Token refreshed successfully");
    }
}