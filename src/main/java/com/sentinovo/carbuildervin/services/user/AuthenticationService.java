package com.sentinovo.carbuildervin.services.user;

import com.sentinovo.carbuildervin.dto.auth.*;
import com.sentinovo.carbuildervin.entities.user.Role;
import com.sentinovo.carbuildervin.entities.user.User;
import com.sentinovo.carbuildervin.exception.ValidationException;
import com.sentinovo.carbuildervin.mapper.user.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserService userService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public User registerUser(String username, String email, String password, String displayName) {
        log.info("Registering new user with username: {}", username);
        
        validateRegistrationData(username, email, password);
        
        User user = userService.createUser(username, email, password, displayName);
        
        Role userRole = roleService.findUserRole();
        user.addRole(userRole);
        
        log.info("Successfully registered user with id: {}", user.getId());
        return user;
    }

    public UserDto registerUser(RegisterRequestDto request) {
        log.info("Registering new user with username: {}", request.getUsername());
        
        validateRegistrationData(request.getUsername(), request.getEmail(), request.getPassword());
        
        UserDto user = userService.createUser(request);
        
        Role userRole = roleService.findUserRole();
        User userEntity = userService.findById(UUID.fromString(user.getId().toString()));
        userEntity.addRole(userRole);
        
        log.info("Successfully registered user with id: {}", user.getId());
        return userMapper.toDto(userEntity);
    }

    public User authenticateUser(String username, String password) {
        log.debug("Authenticating user with username: {}", username);
        
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new ValidationException("Invalid username or password");
        }
        
        User user = userOpt.get();
        
        if (!user.getIsActive()) {
            throw new ValidationException("Account is deactivated");
        }
        
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ValidationException("Invalid username or password");
        }
        
        log.info("Successfully authenticated user: {}", username);
        return user;
    }

    public AuthenticationResponseDto authenticateUser(LoginRequestDto request) {
        log.debug("Authenticating user with credentials: {}", request.getUsernameOrEmail());
        
        String identifier = request.getUsernameOrEmail();
        Optional<User> userOpt;
        
        // Try to find user by username first, then by email
        if (identifier.contains("@")) {
            userOpt = userService.findByEmail(identifier);
        } else {
            userOpt = userService.findByUsername(identifier);
        }
        
        if (userOpt.isEmpty()) {
            throw new ValidationException("Invalid username or password");
        }
        
        User user = userOpt.get();
        
        if (!user.getIsActive()) {
            throw new ValidationException("Account is deactivated");
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ValidationException("Invalid username or password");
        }
        
        log.info("Successfully authenticated user: {}", identifier);
        
        UserDto userDto = userMapper.toDto(user);
        return AuthenticationResponseDto.builder()
                .user(userDto)
                .message("Authentication successful")
                .build();
    }

    @Transactional(readOnly = true)
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        
        String username = authentication.getName();
        return userService.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<UserDto> getCurrentUserDto() {
        return getCurrentUser().map(userMapper::toDto);
    }

    @Transactional(readOnly = true)
    public User getCurrentUserOrThrow() {
        return getCurrentUser()
                .orElseThrow(() -> new ValidationException("No authenticated user found"));
    }

    @Transactional(readOnly = true)
    public UUID getCurrentUserId() {
        return getCurrentUserOrThrow().getId();
    }

    @Transactional(readOnly = true)
    public boolean isCurrentUserAdmin() {
        return getCurrentUser()
                .map(user -> user.getRoles().stream()
                        .anyMatch(role -> "ADMIN".equals(role.getName())))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean isCurrentUserOwner(UUID userId) {
        return getCurrentUser()
                .map(user -> user.getId().equals(userId))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean canAccessUser(UUID userId) {
        return isCurrentUserAdmin() || isCurrentUserOwner(userId);
    }

    public User changeUserPassword(UUID userId, String currentPassword, String newPassword) {
        if (!canAccessUser(userId)) {
            throw new ValidationException("Not authorized to change password for this user");
        }
        
        return userService.changePassword(userId, currentPassword, newPassword);
    }

    public void changeCurrentUserPassword(PasswordChangeDto request) {
        UUID currentUserId = getCurrentUserId();
        userService.changePassword(currentUserId, request);
    }

    public void validateUserAccess(UUID userId, String action) {
        if (!canAccessUser(userId)) {
            throw new ValidationException(String.format("Not authorized to %s for this user", action));
        }
    }

    private void validateRegistrationData(String username, String email, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("username", "Username is required");
        }
        
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("email", "Email is required");
        }
        
        if (password == null || password.length() < 8) {
            throw new ValidationException("password", "Password must be at least 8 characters long");
        }
        
        if (!userService.isUsernameAvailable(username)) {
            throw new ValidationException("username", "Username is already taken");
        }
        
        if (!userService.isEmailAvailable(email)) {
            throw new ValidationException("email", "Email is already registered");
        }
    }
}