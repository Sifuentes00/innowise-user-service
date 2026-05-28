package com.matvey.innowiseuserservice.security;

import com.matvey.innowiseuserservice.util.JwtValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtValidator jwtValidator;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final String VALID_TOKEN = "valid.jwt.token";
    private static final String INVALID_TOKEN = "invalid.token";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_ValidToken_SetsAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtValidator.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtValidator.extractUserId(VALID_TOKEN)).thenReturn(TEST_USER_ID);
        when(jwtValidator.extractRole(VALID_TOKEN)).thenReturn("USER");

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(TEST_USER_ID, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_InvalidToken_DoesNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + INVALID_TOKEN);
        when(jwtValidator.validateToken(INVALID_TOKEN)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_NoAuthorizationHeader_DoesNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInvalidAuthorizationHeader_DoesNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat " + VALID_TOKEN);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_AdminToken_SetsAdminAuthority() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtValidator.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtValidator.extractUserId(VALID_TOKEN)).thenReturn(TEST_USER_ID);
        when(jwtValidator.extractRole(VALID_TOKEN)).thenReturn("ADMIN");

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(1, SecurityContextHolder.getContext().getAuthentication().getAuthorities().size());
        assertEquals("ROLE_ADMIN", SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_TokenValidationException_DoesNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtValidator.validateToken(VALID_TOKEN)).thenReturn(true);
        when(jwtValidator.extractUserId(VALID_TOKEN)).thenThrow(new RuntimeException("Invalid token"));

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_EmptyAuthorizationHeader_DoesNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("");

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }
}
