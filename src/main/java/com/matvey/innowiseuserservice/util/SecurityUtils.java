package com.matvey.innowiseuserservice.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class SecurityUtils {

    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UUID) {
            return (UUID) authentication.getPrincipal();
        }
        throw new IllegalStateException("User not authenticated");
    }

    public static String getCurrentRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            return authentication.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .findFirst()
                    .map(auth -> auth.replace("ROLE_", ""))
                    .orElse(null);
        }
        throw new IllegalStateException("User not authenticated");
    }

    public static boolean isAdmin() {
        String role = getCurrentRole();
        return "ADMIN".equals(role);
    }
}
