package com.sentinovo.carbuildervin.config;

import com.sentinovo.carbuildervin.mcp.security.McpApiKeyAuthenticationFilter;
import com.sentinovo.carbuildervin.mcp.security.OAuthBearerTokenFilter;
import com.sentinovo.carbuildervin.service.user.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final McpApiKeyAuthenticationFilter mcpApiKeyAuthenticationFilter;
    private final OAuthBearerTokenFilter oAuthBearerTokenFilter;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Use standard CSRF handler for form-based submissions (HTMX compatible)
        // This disables XOR encoding which breaks HTML form hidden inputs
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null);

        http
            // Add OAuth Bearer token filter first (checks for Authorization: Bearer header)
            .addFilterBefore(oAuthBearerTokenFilter, UsernamePasswordAuthenticationFilter.class)
            // Add MCP API key filter second (fallback if no Bearer token)
            .addFilterBefore(mcpApiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authz -> authz
                // API endpoints
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/vin/decode").permitAll()
                // MCP endpoints - filter handles authentication via API key or Bearer token
                .requestMatchers("/api/v1/mcp/**").permitAll()
                .requestMatchers("/mcp/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // OAuth endpoints
                .requestMatchers("/.well-known/**").permitAll()
                .requestMatchers("/oauth/token").permitAll()
                .requestMatchers("/oauth/revoke").permitAll()
                .requestMatchers("/oauth/authorize").authenticated()
                .requestMatchers("/oauth/authorize/continue").authenticated()
                .requestMatchers("/oauth/error").permitAll()

                // Static resources
                .requestMatchers("/css/**", "/js/**", "/images/**", "/pico-main/**").permitAll()

                // Public pages
                .requestMatchers("/", "/login", "/signup").permitAll()
                .requestMatchers("/check-username", "/check-password-strength").permitAll()
                .requestMatchers("/fragments/nav").permitAll()

                // Protected pages
                .requestMatchers("/vehicles/**", "/builds/**", "/parts/**", "/vin/**").authenticated()
                .requestMatchers("/api/v1/**").authenticated()

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("usernameOrEmail")
                .passwordParameter("password")
                .defaultSuccessUrl("/builds", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("car-builder-vin-remember-me")
                .tokenValiditySeconds(86400 * 30) // 30 days
                .userDetailsService(userDetailsService)
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            .csrf(csrf -> csrf
                .csrfTokenRequestHandler(requestHandler)
                .ignoringRequestMatchers("/api/v1/**")
                .ignoringRequestMatchers("/oauth/token", "/oauth/revoke")
                .ignoringRequestMatchers("/mcp/**")
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .contentTypeOptions(content -> {})
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                )
            );
        
        return http.build();
    }
}