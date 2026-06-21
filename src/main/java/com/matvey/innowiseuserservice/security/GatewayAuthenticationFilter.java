package com.matvey.innowiseuserservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@Component
public class GatewayAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(GatewayAuthenticationFilter.class);

    private static final Set<String> PUBLIC_PATHS = Set.of("/internal/");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            filterChain.doFilter(request, response);
            return;
        }

        String userIdHeader = request.getHeader("X-User-Id");
        String roleHeader = request.getHeader("X-User-Role");

        if (userIdHeader == null || roleHeader == null) {
            logger.warn("Missing gateway headers: X-User-Id or X-User-Role");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing gateway headers");
            return;
        }

        try {
            UUID userId = UUID.fromString(userIdHeader);
            String role = roleHeader;

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                    );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            logger.debug("Successfully authenticated user: {} with role: {} from gateway", userId, role);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid X-User-Id header format: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid X-User-Id header");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
