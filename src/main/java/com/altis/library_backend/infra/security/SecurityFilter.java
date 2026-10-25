
package com.altis.library_backend.infra.security;

import com.altis.library_backend.auth.services.TokenService;
import com.altis.library_backend.users.models.entities.UserEntity;
import com.altis.library_backend.users.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    public SecurityFilter(
            TokenService tokenService,
            UserRepository userRepository
    ) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = recoveryToken(request);

        if (token != null) {
            String subject = tokenService.validationToken(token);

            if (subject == null || subject.isBlank()) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Invalid or expired token"
                );
                return;
            }

            UserEntity user = userRepository
                    .findByEmail(subject)
                    .orElse(null);

            if (user == null) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "User not found"
                );
                return;
            }

            var authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            user.getAuthorities()
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String recoveryToken(HttpServletRequest request) {
        String authHeader = request.getHeader(
                HttpHeaders.AUTHORIZATION
        );

        if (authHeader == null) {
            return null;
        }

        if (!authHeader.startsWith("Bearer ")) {
            return null;
        }

        return authHeader.substring(7);
    }
}
