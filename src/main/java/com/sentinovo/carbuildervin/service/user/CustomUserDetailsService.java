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

import java.util.Collection;
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

    public static class CustomUserPrincipal implements UserDetails {
        private final User user;

        public CustomUserPrincipal(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                    .collect(Collectors.toList());
        }

        @Override
        public String getPassword() {
            return user.getPasswordHash();
        }

        @Override
        public String getUsername() {
            return user.getUsername();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return user.getIsActive();
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return user.getIsActive();
        }

        public User getUser() {
            return user;
        }
    }
}