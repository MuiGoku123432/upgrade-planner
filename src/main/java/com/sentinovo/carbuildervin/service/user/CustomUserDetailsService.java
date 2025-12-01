package com.sentinovo.carbuildervin.service.user;

import com.sentinovo.carbuildervin.entities.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user;
        
        // Try to find by username first, then by email
        // Use the method that eagerly fetches roles to avoid LazyInitializationException
        if (usernameOrEmail.contains("@")) {
            user = userService.findByEmail(usernameOrEmail)
                    .map(u -> userService.findByUsernameWithRoles(u.getUsername()))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + usernameOrEmail));
        } else {
            user = userService.findByUsernameWithRoles(usernameOrEmail);
        }

        return new CustomUserPrincipal(user);
    }

    public static class CustomUserPrincipal implements UserDetails, Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private final UUID userId;
        private final String username;
        private final String email;
        private final String passwordHash;
        private final boolean isActive;
        private final Set<String> roles;

        public CustomUserPrincipal(User user) {
            this.userId = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.passwordHash = user.getPasswordHash();
            this.isActive = user.getIsActive();
            this.roles = user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toSet());
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return roles.stream()
                    .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName))
                    .collect(Collectors.toList());
        }

        @Override
        public String getPassword() {
            return passwordHash;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return isActive;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return isActive;
        }

        public UUID getUserId() {
            return userId;
        }

        public String getEmail() {
            return email;
        }
    }
}