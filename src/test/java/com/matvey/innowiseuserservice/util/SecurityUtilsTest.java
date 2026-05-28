package com.matvey.innowiseuserservice.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilsTest {

    private static final UUID TEST_USER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetCurrentUserId_Authenticated_ReturnsUserId() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                TEST_USER_ID,
                null,
                Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UUID userId = SecurityUtils.getCurrentUserId();

        assertEquals(TEST_USER_ID, userId);
    }

    @Test
    void testGetCurrentUserId_NotAuthenticated_ThrowsException() {
        assertThrows(IllegalStateException.class, SecurityUtils::getCurrentUserId);
    }

    @Test
    void testGetCurrentUserId_AuthenticationPrincipalNotUUID_ThrowsException() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "not-a-uuid",
                null,
                Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThrows(IllegalStateException.class, SecurityUtils::getCurrentUserId);
    }

    @Test
    void testGetCurrentRole_UserRole_ReturnsUser() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                TEST_USER_ID,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String role = SecurityUtils.getCurrentRole();

        assertEquals("USER", role);
    }

    @Test
    void testGetCurrentRole_AdminRole_ReturnsAdmin() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                TEST_USER_ID,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String role = SecurityUtils.getCurrentRole();

        assertEquals("ADMIN", role);
    }

    @Test
    void testGetCurrentRole_NotAuthenticated_ThrowsException() {
        assertThrows(IllegalStateException.class, SecurityUtils::getCurrentRole);
    }

    @Test
    void testGetCurrentRole_NoAuthorities_ReturnsNull() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                TEST_USER_ID,
                null,
                Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String role = SecurityUtils.getCurrentRole();

        assertNull(role);
    }

    @Test
    void testIsAdmin_AdminRole_ReturnsTrue() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                TEST_USER_ID,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        boolean isAdmin = SecurityUtils.isAdmin();

        assertTrue(isAdmin);
    }

    @Test
    void testIsAdmin_UserRole_ReturnsFalse() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                TEST_USER_ID,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        boolean isAdmin = SecurityUtils.isAdmin();

        assertFalse(isAdmin);
    }

    @Test
    void testIsAdmin_NotAuthenticated_ThrowsException() {
        assertThrows(IllegalStateException.class, SecurityUtils::isAdmin);
    }
}
