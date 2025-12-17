# Google OAuth Implementation Plan

> **Status**: Planned (not yet implemented)
> **Estimated Effort**: 2-3 hours
> **Prerequisites**: Google Cloud Console OAuth credentials

## Overview

Add optional Google OAuth login alongside existing username/password authentication. Users can:
- Sign up with email/password (existing)
- Sign up with Google (new)
- Sign in with email/password (existing)
- Sign in with Google (new)

**Account Linking Strategy**: Auto-link by email. If a Google user's email matches an existing account, they'll be linked automatically and logged in.

---

## Prerequisites

### 1. Create Google OAuth Credentials

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing
3. Navigate to **APIs & Services > Credentials**
4. Click **Create Credentials > OAuth 2.0 Client IDs**
5. Configure OAuth consent screen if prompted
6. Application type: **Web application**
7. Add authorized redirect URI:
   - Production: `https://builder.sentinovo.ai/login/oauth2/code/google`
   - Local dev: `http://localhost:8080/login/oauth2/code/google`
8. Copy **Client ID** and **Client Secret**

---

## Implementation Steps

### Step 1: Add Dependency

**File**: `pom.xml`

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

---

### Step 2: Add Configuration

**File**: `src/main/resources/application.properties`

```properties
# ================================
# Google OAuth2 Configuration
# ================================
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID:}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET:}
spring.security.oauth2.client.registration.google.scope=email,profile
```

**File**: `.env.example` (add these lines)

```properties
# ================================
# Google OAuth (Optional)
# ================================
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
```

---

### Step 3: Database Migration

**File**: `src/main/resources/db/migration/V4__Add_oauth_fields.sql`

```sql
-- Add OAuth support fields to app_user table
ALTER TABLE app_user
    ADD COLUMN oauth_provider VARCHAR(50),
    ADD COLUMN oauth_id VARCHAR(255);

-- Make password_hash nullable for OAuth-only users
ALTER TABLE app_user
    ALTER COLUMN password_hash DROP NOT NULL;

-- Create index for OAuth lookups
CREATE INDEX idx_app_user_oauth ON app_user(oauth_provider, oauth_id);

-- Add unique constraint for OAuth provider + ID combination
ALTER TABLE app_user
    ADD CONSTRAINT uq_oauth_provider_id UNIQUE (oauth_provider, oauth_id);
```

---

### Step 4: Update User Entity

**File**: `src/main/java/com/sentinovo/carbuildervin/entities/user/User.java`

Add these fields:

```java
@Column(name = "oauth_provider", length = 50)
private String oauthProvider;

@Column(name = "oauth_id", length = 255)
private String oauthId;
```

Update the `@NotBlank` annotation on `passwordHash` to allow null for OAuth users:

```java
// Change from @NotBlank to allow OAuth users without password
@Size(max = 255, message = "Password hash must not exceed 255 characters")
@Column(name = "password_hash", length = 255)
private String passwordHash;
```

Add helper method:

```java
public boolean isOAuthUser() {
    return oauthProvider != null && oauthId != null;
}
```

---

### Step 5: Create OAuth2 User Service

**File**: `src/main/java/com/sentinovo/carbuildervin/service/user/CustomOAuth2UserService.java`

```java
package com.sentinovo.carbuildervin.service.user;

import com.sentinovo.carbuildervin.entities.user.Role;
import com.sentinovo.carbuildervin.entities.user.User;
import com.sentinovo.carbuildervin.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google"
        String oauthId = oAuth2User.getAttribute("sub"); // Google's unique user ID
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        log.info("OAuth2 login attempt - provider: {}, email: {}", provider, email);

        // Find or create user
        User user = findOrCreateUser(provider, oauthId, email, name);

        // Build authorities from user roles
        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toSet());

        // Return OAuth2User with our user's authorities
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("userId", user.getId().toString());
        attributes.put("username", user.getUsername());

        return new DefaultOAuth2User(authorities, attributes, "email");
    }

    private User findOrCreateUser(String provider, String oauthId, String email, String name) {
        // First, try to find by OAuth provider + ID
        Optional<User> existingOAuthUser = userRepository.findByOauthProviderAndOauthId(provider, oauthId);
        if (existingOAuthUser.isPresent()) {
            log.info("Found existing OAuth user: {}", existingOAuthUser.get().getUsername());
            return existingOAuthUser.get();
        }

        // Next, try to find by email (auto-link)
        Optional<User> existingEmailUser = userRepository.findByEmail(email);
        if (existingEmailUser.isPresent()) {
            User user = existingEmailUser.get();
            log.info("Linking OAuth to existing user by email: {}", email);
            user.setOauthProvider(provider);
            user.setOauthId(oauthId);
            return userRepository.save(user);
        }

        // Create new user
        log.info("Creating new OAuth user for email: {}", email);
        String username = generateUniqueUsername(email, name);

        User newUser = User.builder()
                .username(username)
                .email(email)
                .displayName(name)
                .oauthProvider(provider)
                .oauthId(oauthId)
                .isActive(true)
                .build();

        newUser = userRepository.save(newUser);

        // Assign default role
        Role userRole = roleService.findUserRole();
        newUser.addRole(userRole);

        return userRepository.save(newUser);
    }

    private String generateUniqueUsername(String email, String name) {
        // Try email prefix first
        String baseUsername = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
        String username = baseUsername;

        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter++;
        }

        return username;
    }
}
```

---

### Step 6: Add Repository Method

**File**: `src/main/java/com/sentinovo/carbuildervin/repository/user/UserRepository.java`

Add this method:

```java
Optional<User> findByOauthProviderAndOauthId(String oauthProvider, String oauthId);
```

---

### Step 7: Update Security Config

**File**: `src/main/java/com/sentinovo/carbuildervin/config/SecurityConfig.java`

Add the OAuth2UserService as a dependency:

```java
private final CustomOAuth2UserService customOAuth2UserService;
```

Update the `filterChain` method to add OAuth2 login:

```java
http
    .authorizeHttpRequests(authz -> authz
        // ... existing matchers ...
        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
        // ... rest of existing config ...
    )
    .formLogin(form -> form
        // ... existing form login config ...
    )
    .oauth2Login(oauth2 -> oauth2
        .loginPage("/login")
        .userInfoEndpoint(userInfo -> userInfo
            .userService(customOAuth2UserService)
        )
        .defaultSuccessUrl("/builds", true)
        .failureUrl("/login?error=oauth")
    )
    // ... rest of existing config ...
```

---

### Step 8: Update Login Page

**File**: `src/main/resources/templates/auth/login.html`

Add Google sign-in button after the login form:

```html
<!-- Divider -->
<div style="text-align: center; margin: 1.5rem 0;">
    <span style="color: var(--muted-color);">or</span>
</div>

<!-- Google Sign In -->
<a href="/oauth2/authorization/google" class="button outline" style="width: 100%; display: flex; align-items: center; justify-content: center; gap: 0.5rem;">
    <svg width="18" height="18" viewBox="0 0 18 18" xmlns="http://www.w3.org/2000/svg">
        <path fill="#4285F4" d="M17.64 9.2c0-.637-.057-1.251-.164-1.84H9v3.481h4.844c-.209 1.125-.843 2.078-1.796 2.717v2.258h2.908c1.702-1.567 2.684-3.874 2.684-6.615z"/>
        <path fill="#34A853" d="M9 18c2.43 0 4.467-.806 5.956-2.18l-2.908-2.259c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332C2.438 15.983 5.482 18 9 18z"/>
        <path fill="#FBBC05" d="M3.964 10.71c-.18-.54-.282-1.117-.282-1.71s.102-1.17.282-1.71V4.958H.957C.347 6.173 0 7.548 0 9s.348 2.827.957 4.042l3.007-2.332z"/>
        <path fill="#EA4335" d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0 5.482 0 2.438 2.017.957 4.958L3.964 7.29C4.672 5.163 6.656 3.58 9 3.58z"/>
    </svg>
    Sign in with Google
</a>
```

---

### Step 9: Update Signup Page (Optional)

**File**: `src/main/resources/templates/auth/signup.html`

Add similar Google button for signup flow.

---

## Environment Variables Summary

Add to your `.env` file:

```properties
GOOGLE_CLIENT_ID=your-client-id-from-google-console
GOOGLE_CLIENT_SECRET=your-client-secret-from-google-console
```

Add to `compose.prod.yaml` environment section:

```yaml
- GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID:}
- GOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET:}
```

---

## Testing Checklist

- [ ] New user signs up with Google → account created, logged in
- [ ] Existing user (password account) signs in with Google → accounts linked, logged in
- [ ] Google user signs in again → recognized, logged in
- [ ] Traditional login still works
- [ ] Traditional signup still works
- [ ] User without Google credentials can still use the app normally

---

## Security Considerations

1. **Email verification**: Google verifies email ownership, so auto-linking by email is safe
2. **OAuth-only users**: Users who sign up via Google won't have a password (password_hash is null)
3. **Account takeover protection**: Only Google-verified emails can trigger auto-linking
4. **Session handling**: OAuth users get the same session-based auth as password users

---

## Future Enhancements

- Add other OAuth providers (GitHub, Apple, Microsoft)
- Allow users to link/unlink OAuth providers in account settings
- Add "Set password" option for OAuth-only users who want password fallback
