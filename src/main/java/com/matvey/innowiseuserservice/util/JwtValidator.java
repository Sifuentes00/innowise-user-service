package com.matvey.innowiseuserservice.util;

import com.matvey.innowiseuserservice.dto.PublicKeyResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class JwtValidator {

    private static final Logger logger = LoggerFactory.getLogger(JwtValidator.class);

    @Value("${auth.service.url}")
    private String authServiceUrl;

    private PublicKey publicKey;
    private final RestTemplate restTemplate;

    public JwtValidator(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedRate = 3600000, initialDelay = 30000)
    public void refreshPublicKey() {
        try {
            fetchPublicKey();
            logger.info("Public key refreshed successfully");
        } catch (Exception e) {
            logger.warn("Failed to refresh public key: {}", e.getMessage());
        }
    }

    private void fetchPublicKey() {
        try {
            String url = authServiceUrl + "/api/auth/public-key";
            PublicKeyResponse response = restTemplate.getForObject(url, PublicKeyResponse.class);
            
            if (response != null && response.getPublicKey() != null) {
                String publicKeyPem = response.getPublicKey()
                        .replace("-----BEGIN PUBLIC KEY-----", "")
                        .replace("-----END PUBLIC KEY-----", "")
                        .replaceAll("\\s", "");

                byte[] keyBytes = Base64.getDecoder().decode(publicKeyPem);
                X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                this.publicKey = keyFactory.generatePublic(spec);
                
                logger.info("Public key fetched successfully from Auth Service");
            }
        } catch (Exception e) {
            logger.warn("Failed to fetch public key from Auth Service: {}", e.getMessage());
        }
    }

    private void ensurePublicKeyLoaded() {
        if (publicKey == null) {
            fetchPublicKey();
            if (publicKey == null) {
                throw new IllegalStateException("Public key not available. Auth Service may be down.");
            }
        }
    }

    public boolean validateToken(String token) {
        try {
            ensurePublicKeyLoaded();
            Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public java.util.UUID extractUserId(String token) {
        ensurePublicKeyLoaded();
        Claims claims = parseClaims(token);
        return java.util.UUID.fromString(claims.getSubject());
    }

    public String extractRole(String token) {
        ensurePublicKeyLoaded();
        Claims claims = parseClaims(token);
        return claims.get("role", String.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
