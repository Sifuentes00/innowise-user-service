package com.matvey.innowiseuserservice.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Configuration
@Profile("test")
public class TestJwtValidator {

    private static final SecretKey TEST_KEY = Keys.hmacShaKeyFor("test-secret-key-for-jwt-validation-in-tests".getBytes(StandardCharsets.UTF_8));

    @Bean
    @Primary
    public JwtValidator jwtValidator() {
        return new JwtValidator(null) {
            @Override
            public boolean validateToken(String token) {
                try {
                    Jwts.parser()
                            .verifyWith(TEST_KEY)
                            .build()
                            .parseSignedClaims(token);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            public UUID extractUserId(String token) {
                try {
                    String subject = Jwts.parser()
                            .verifyWith(TEST_KEY)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload()
                            .getSubject();
                    return UUID.fromString(subject);
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            public String extractRole(String token) {
                try {
                    return Jwts.parser()
                            .verifyWith(TEST_KEY)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload()
                            .get("role", String.class);
                } catch (Exception e) {
                    return "USER";
                }
            }
        };
    }
}
