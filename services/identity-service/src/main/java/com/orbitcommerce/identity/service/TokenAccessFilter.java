package com.orbitcommerce.identity.service;

import com.orbitcommerce.identity.enums.UserStatus;
import com.orbitcommerce.identity.model.User;
import com.orbitcommerce.identity.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class TokenAccessFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    public TokenAccessFilter(TokenService tokenService, UserRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        ArrayList<UserStatus> statuses = new ArrayList<>();
        statuses.add(UserStatus.BLOCKED);
        statuses.add(UserStatus.DELETED);

        String token = tokenService.extractTokenFromRequest(request);

        if (token != null) {
            String email = tokenService.tokenVerify(token);

            if (email != null) {
                User user = userRepository.findByEmailIgnoreCaseAndStatusNotIn(email, statuses).orElseThrow();
                Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        AntPathMatcher pathMatcher = new AntPathMatcher();

        return pathMatcher.match("/api/v1/v3/api-docs/**", path) ||
                pathMatcher.match("/api/v1/swagger-ui/**", path) ||
                pathMatcher.match("/api/v1/actuator/**", path) ||
                pathMatcher.match("/api/v1/users/register", path) ||
                pathMatcher.match("/api/v1/auth/login", path);
    }

}
