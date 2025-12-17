package com.sentinovo.carbuildervin.service.user;

import com.sentinovo.carbuildervin.dto.auth.*;
import com.sentinovo.carbuildervin.dto.common.PageResponseDto;
import com.sentinovo.carbuildervin.entities.user.User;
import com.sentinovo.carbuildervin.exception.DuplicateResourceException;
import com.sentinovo.carbuildervin.exception.ResourceNotFoundException;
import com.sentinovo.carbuildervin.exception.ValidationException;
import com.sentinovo.carbuildervin.mapper.user.UserMapper;
import com.sentinovo.carbuildervin.repository.user.UserRepository;
import com.sentinovo.carbuildervin.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(UUID id) {
        User user = findById(id);
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(UUID id) {
        User user = findById(id);
        return userMapper.toProfileDto(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public User findByUsernameWithRoles(String username) {
        return userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
    }

    @Transactional(readOnly = true)
    public Page<User> findAllActiveUsers(Pageable pageable) {
        return userRepository.findAllActive(pageable);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<UserDto> getAllActiveUsers(Pageable pageable) {
        Page<User> page = userRepository.findAllActive(pageable);
        return userMapper.toPageDto(page);
    }

    @Transactional(readOnly = true)
    public Page<User> searchActiveUsers(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAllActiveUsers(pageable);
        }
        return userRepository.searchActiveUsers(searchTerm.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<UserDto> searchUsers(String searchTerm, Pageable pageable) {
        Page<User> page;
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            page = findAllActiveUsers(pageable);
        } else {
            page = userRepository.searchActiveUsers(searchTerm.trim(), pageable);
        }
        return userMapper.toPageDto(page);
    }

    @Transactional(readOnly = true)
    public long countActiveUsers() {
        return userRepository.countActiveUsers();
    }

    public User createUser(String username, String email, String password, String displayName) {
        log.info("Creating new user with username: {}", username);
        
        validateUserCreation(username, email);
        ValidationUtils.validatePassword(password);
        
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .displayName(displayName)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Successfully created user with id: {}", savedUser.getId());
        return savedUser;
    }

    public UserDto createUser(RegisterRequestDto request) {
        log.info("Creating new user with username: {}", request.getUsername());
        
        validateUserCreation(request.getUsername(), request.getEmail());
        ValidationUtils.validatePassword(request.getPassword());
        
        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);

        User savedUser = userRepository.save(user);
        log.info("Successfully created user with id: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    public User updateUser(UUID userId, String email, String displayName) {
        log.info("Updating user with id: {}", userId);
        
        User user = findById(userId);
        
        if (email != null && !email.equals(user.getEmail())) {
            validateEmailUniqueness(email, userId);
            user.setEmail(email);
        }
        
        if (displayName != null) {
            user.setDisplayName(displayName);
        }

        User savedUser = userRepository.save(user);
        log.info("Successfully updated user with id: {}", savedUser.getId());
        return savedUser;
    }

    public UserDto updateUser(UUID userId, UserUpdateDto updateDto) {
        log.info("Updating user with id: {}", userId);
        
        User user = findById(userId);
        
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(user.getEmail())) {
            validateEmailUniqueness(updateDto.getEmail(), userId);
        }
        
        userMapper.updateEntity(user, updateDto);
        User savedUser = userRepository.save(user);
        
        log.info("Successfully updated user with id: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    public User changePassword(UUID userId, String currentPassword, String newPassword) {
        log.info("Changing password for user with id: {}", userId);
        
        User user = findById(userId);
        
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new ValidationException("currentPassword", "Current password is incorrect");
        }
        
        ValidationUtils.validatePassword(newPassword);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        
        User savedUser = userRepository.save(user);
        log.info("Successfully changed password for user with id: {}", savedUser.getId());
        return savedUser;
    }

    public void changePassword(UUID userId, PasswordChangeDto request) {
        log.info("Changing password for user with id: {}", userId);
        
        User user = findById(userId);
        
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ValidationException("currentPassword", "Current password is incorrect");
        }
        
        ValidationUtils.validatePassword(request.getNewPassword());
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        
        userRepository.save(user);
        log.info("Successfully changed password for user with id: {}", userId);
    }

    public User activateUser(UUID userId) {
        log.info("Activating user with id: {}", userId);
        
        User user = findById(userId);
        user.setIsActive(true);
        
        User savedUser = userRepository.save(user);
        log.info("Successfully activated user with id: {}", savedUser.getId());
        return savedUser;
    }

    public UserDto activateUserDto(UUID userId) {
        User user = activateUser(userId);
        return userMapper.toDto(user);
    }

    public User deactivateUser(UUID userId) {
        log.info("Deactivating user with id: {}", userId);
        
        User user = findById(userId);
        user.setIsActive(false);
        
        User savedUser = userRepository.save(user);
        log.info("Successfully deactivated user with id: {}", savedUser.getId());
        return savedUser;
    }

    public UserDto deactivateUserDto(UUID userId) {
        User user = deactivateUser(userId);
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    private void validateUserCreation(String username, String email) {
        ValidationUtils.validateUsername(username);
        
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("User", "username", username);
        }
        
        if (email != null) {
            ValidationUtils.validateEmail(email);
            if (userRepository.existsByEmail(email)) {
                throw new DuplicateResourceException("User", "email", email);
            }
        }
    }

    private void validateEmailUniqueness(String email, UUID userId) {
        ValidationUtils.validateEmail(email);
        if (userRepository.existsByEmailAndIdNot(email, userId)) {
            throw new DuplicateResourceException("User", "email", email);
        }
    }

    public UserDto findByUsernameDto(String username) {
        User user = findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return userMapper.toDto(user);
    }


    public void deleteUser(UUID userId) {
        log.info("Deleting user with id: {}", userId);
        User user = findById(userId);
        user.setIsActive(false);
        userRepository.save(user);
        log.info("Successfully deleted user with id: {}", userId);
    }

    // ========================================
    // MCP API Key Management
    // ========================================

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int API_KEY_LENGTH = 32; // 32 bytes = 64 hex characters

    /**
     * Generate a new MCP API key for the user. This revokes any existing key.
     * @param userId the user ID
     * @return the new API key (plaintext, shown only once)
     */
    public String generateMcpApiKey(UUID userId) {
        log.info("Generating new MCP API key for user: {}", userId);

        User user = findById(userId);

        // Generate a new random API key
        byte[] keyBytes = new byte[API_KEY_LENGTH];
        SECURE_RANDOM.nextBytes(keyBytes);
        String apiKey = HexFormat.of().formatHex(keyBytes);

        // Store the key and timestamp
        user.setMcpApiKey(apiKey);
        user.setMcpApiKeyCreatedAt(OffsetDateTime.now());

        userRepository.save(user);
        log.info("Successfully generated MCP API key for user: {}", userId);

        return apiKey;
    }

    /**
     * Revoke the MCP API key for the user.
     * @param userId the user ID
     */
    public void revokeMcpApiKey(UUID userId) {
        log.info("Revoking MCP API key for user: {}", userId);

        User user = findById(userId);
        user.setMcpApiKey(null);
        user.setMcpApiKeyCreatedAt(null);

        userRepository.save(user);
        log.info("Successfully revoked MCP API key for user: {}", userId);
    }

    /**
     * Check if user has an MCP API key.
     * @param userId the user ID
     * @return true if user has an API key
     */
    @Transactional(readOnly = true)
    public boolean hasMcpApiKey(UUID userId) {
        User user = findById(userId);
        return user.getMcpApiKey() != null;
    }

    /**
     * Get masked MCP API key info for display.
     * @param userId the user ID
     * @return masked key (first 8 chars + "..." + last 4 chars) or null if no key
     */
    @Transactional(readOnly = true)
    public String getMaskedMcpApiKey(UUID userId) {
        User user = findById(userId);
        String key = user.getMcpApiKey();
        if (key == null || key.length() < 12) {
            return null;
        }
        return key.substring(0, 8) + "..." + key.substring(key.length() - 4);
    }

    /**
     * Get the creation timestamp of the MCP API key.
     * @param userId the user ID
     * @return creation timestamp or null if no key
     */
    @Transactional(readOnly = true)
    public OffsetDateTime getMcpApiKeyCreatedAt(UUID userId) {
        User user = findById(userId);
        return user.getMcpApiKeyCreatedAt();
    }

    /**
     * Find user by MCP API key (for authentication).
     * @param apiKey the API key
     * @return the user if found and active
     */
    @Transactional(readOnly = true)
    public Optional<User> findByMcpApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByMcpApiKeyWithRoles(apiKey);
    }

}