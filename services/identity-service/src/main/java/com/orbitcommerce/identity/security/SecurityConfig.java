package com.orbitcommerce.identity.security;

import com.orbitcommerce.identity.service.TokenAccessFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final TokenAccessFilter tokenAccessFilter;

    public SecurityConfig(TokenAccessFilter tokenAccessFilter) {
        this.tokenAccessFilter = tokenAccessFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(req -> {
                    req.dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll();
                    req.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll();
                    req.requestMatchers("/actuator/**").permitAll();
                    req.requestMatchers("/users/register", "/auth/login").permitAll();
                    req.requestMatchers("/users/delete/{id}").hasAnyRole("CUSTOMER", "ADMIN");
                    req.requestMatchers(HttpMethod.PATCH, "/users/change-password")
                            .hasAnyRole("CUSTOMER", "SELLER", "ADMIN");
                    req.requestMatchers(HttpMethod.PATCH, "/users/{id}/block").hasRole("ADMIN");
                    req.requestMatchers(HttpMethod.PATCH, "/users/{id}/unlock").hasRole("ADMIN");
                    req.anyRequest().authenticated();
                })
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(tokenAccessFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public RoleHierarchy rolesHierarchy(){
        String hierarchy = "ROLE_ADMIN > ROLE_SELLER\n" +
                "ROLE_ADMIN > ROLE_CUSTOMER";
        return RoleHierarchyImpl.fromHierarchy(hierarchy);
    }

}
