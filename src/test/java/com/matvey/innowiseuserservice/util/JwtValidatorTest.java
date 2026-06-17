package com.matvey.innowiseuserservice.util;

import com.matvey.innowiseuserservice.dto.PublicKeyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtValidatorTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private JwtValidator jwtValidator;

    private PublicKey publicKey;
    private String validToken;

    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(jwtValidator, "authServiceUrl", "http://localhost:8081");

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        publicKey = keyPair.getPublic();

        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKey.getEncoded());
        String publicKeyPem = Base64.getEncoder().encodeToString(spec.getEncoded());

        PublicKeyResponse publicKeyResponse = new PublicKeyResponse();
        publicKeyResponse.setPublicKey("-----BEGIN PUBLIC KEY-----" + publicKeyPem + "-----END PUBLIC KEY-----");

        when(restTemplate.getForObject(any(String.class), eq(PublicKeyResponse.class)))
                .thenReturn(publicKeyResponse);

        ReflectionTestUtils.invokeMethod(jwtValidator, "fetchPublicKey");

        validToken = io.jsonwebtoken.Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claim("role", "USER")
                .claim("token-type", "access")
                .signWith(keyPair.getPrivate())
                .compact();
    }

    @Test
    void testValidateToken_ValidToken_ReturnsTrue() {
        boolean result = jwtValidator.validateToken(validToken);
        assertTrue(result);
    }

    @Test
    void testValidateToken_InvalidToken_ReturnsFalse() {
        boolean result = jwtValidator.validateToken("invalid.token.here");
        assertFalse(result);
    }

    @Test
    void testValidateToken_NullToken_ReturnsFalse() {
        boolean result = jwtValidator.validateToken(null);
        assertFalse(result);
    }

    @Test
    void testExtractUserId_ValidToken_ReturnsUserId() throws Exception {
        UUID userId = UUID.randomUUID();
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        String token = io.jsonwebtoken.Jwts.builder()
                .subject(userId.toString())
                .claim("role", "USER")
                .claim("token-type", "access")
                .signWith(keyPair.getPrivate())
                .compact();

        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyPair.getPublic().getEncoded());
        String publicKeyPem = Base64.getEncoder().encodeToString(spec.getEncoded());

        PublicKeyResponse publicKeyResponse = new PublicKeyResponse();
        publicKeyResponse.setPublicKey("-----BEGIN PUBLIC KEY-----" + publicKeyPem + "-----END PUBLIC KEY-----");

        when(restTemplate.getForObject(any(String.class), eq(PublicKeyResponse.class)))
                .thenReturn(publicKeyResponse);

        ReflectionTestUtils.invokeMethod(jwtValidator, "fetchPublicKey");

        UUID extractedUserId = jwtValidator.extractUserId(token);
        assertEquals(userId, extractedUserId);
    }

    @Test
    void testExtractRole_ValidToken_ReturnsRole() {
        String role = jwtValidator.extractRole(validToken);
        assertEquals("USER", role);
    }

    @Test
    void testExtractRole_AdminToken_ReturnsAdmin() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        String adminToken = io.jsonwebtoken.Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claim("role", "ADMIN")
                .claim("token-type", "access")
                .signWith(keyPair.getPrivate())
                .compact();

        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyPair.getPublic().getEncoded());
        String publicKeyPem = Base64.getEncoder().encodeToString(spec.getEncoded());

        PublicKeyResponse publicKeyResponse = new PublicKeyResponse();
        publicKeyResponse.setPublicKey("-----BEGIN PUBLIC KEY-----" + publicKeyPem + "-----END PUBLIC KEY-----");

        when(restTemplate.getForObject(any(String.class), eq(PublicKeyResponse.class)))
                .thenReturn(publicKeyResponse);

        ReflectionTestUtils.invokeMethod(jwtValidator, "fetchPublicKey");

        String role = jwtValidator.extractRole(adminToken);
        assertEquals("ADMIN", role);
    }

    @Test
    void testEnsurePublicKeyLoaded_PublicKeyNotSet_FetchesKey() {
        ReflectionTestUtils.setField(jwtValidator, "publicKey", null);

        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKey.getEncoded());
        String publicKeyPem = Base64.getEncoder().encodeToString(spec.getEncoded());

        PublicKeyResponse publicKeyResponse = new PublicKeyResponse();
        publicKeyResponse.setPublicKey("-----BEGIN PUBLIC KEY-----" + publicKeyPem + "-----END PUBLIC KEY-----");

        when(restTemplate.getForObject(any(String.class), eq(PublicKeyResponse.class)))
                .thenReturn(publicKeyResponse);

        assertDoesNotThrow(() -> jwtValidator.validateToken(validToken));
        assertNotNull(ReflectionTestUtils.getField(jwtValidator, "publicKey"));
    }

    @Test
    void testEnsurePublicKeyLoaded_AuthServiceUnavailable_ThrowsException() {
        ReflectionTestUtils.setField(jwtValidator, "publicKey", null);

        when(restTemplate.getForObject(any(String.class), eq(PublicKeyResponse.class)))
                .thenReturn(null);

        assertThrows(IllegalStateException.class, () -> jwtValidator.extractUserId(validToken));
    }

    @Test
    void testExtractTokenType_AccessToken_ReturnsAccess() {
        String tokenType = jwtValidator.extractTokenType(validToken);
        assertEquals("access", tokenType);
    }

    @Test
    void testExtractTokenType_RefreshToken_ReturnsRefresh() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        String refreshToken = io.jsonwebtoken.Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claim("token-type", "refresh")
                .signWith(keyPair.getPrivate())
                .compact();

        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyPair.getPublic().getEncoded());
        String publicKeyPem = Base64.getEncoder().encodeToString(spec.getEncoded());

        PublicKeyResponse publicKeyResponse = new PublicKeyResponse();
        publicKeyResponse.setPublicKey("-----BEGIN PUBLIC KEY-----" + publicKeyPem + "-----END PUBLIC KEY-----");

        when(restTemplate.getForObject(any(String.class), eq(PublicKeyResponse.class)))
                .thenReturn(publicKeyResponse);

        ReflectionTestUtils.invokeMethod(jwtValidator, "fetchPublicKey");

        String tokenType = jwtValidator.extractTokenType(refreshToken);
        assertEquals("refresh", tokenType);
    }

    @Test
    void testValidateToken_RefreshToken_ThrowsInvalidTokenException() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        String refreshToken = io.jsonwebtoken.Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claim("token-type", "refresh")
                .signWith(keyPair.getPrivate())
                .compact();

        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyPair.getPublic().getEncoded());
        String publicKeyPem = Base64.getEncoder().encodeToString(spec.getEncoded());

        PublicKeyResponse publicKeyResponse = new PublicKeyResponse();
        publicKeyResponse.setPublicKey("-----BEGIN PUBLIC KEY-----" + publicKeyPem + "-----END PUBLIC KEY-----");

        when(restTemplate.getForObject(any(String.class), eq(PublicKeyResponse.class)))
                .thenReturn(publicKeyResponse);

        ReflectionTestUtils.invokeMethod(jwtValidator, "fetchPublicKey");

        assertThrows(com.matvey.innowiseuserservice.exception.InvalidTokenException.class, () -> jwtValidator.validateToken(refreshToken));
    }
}
