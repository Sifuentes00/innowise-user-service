package com.matvey.innowiseuserservice.integration;

import com.matvey.innowiseuserservice.dto.UserCreateRequest;
import com.matvey.innowiseuserservice.dto.UserDto;
import com.matvey.innowiseuserservice.entity.User;
import com.matvey.innowiseuserservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@org.springframework.test.context.ActiveProfiles("test")
class InternalEndpointsIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16-alpine"))
            .withReuse(true);

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    private RestTemplate restTemplate;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        restTemplate = new RestTemplate(factory);
        baseUrl = "http://localhost:" + port;
        userRepository.deleteAll();
    }

    @Test
    void testCreateUserInternal() {
        UUID userId = UUID.randomUUID();
        UserCreateRequest request = new UserCreateRequest();
        request.setUserId(userId);
        request.setName("John");
        request.setSurname("Doe");
        request.setBirthDate(LocalDate.of(1990, 1, 1));
        request.setEmail("john@example.com");

        ResponseEntity<UserDto> response = restTemplate.postForEntity(
                baseUrl + "/internal/users",
                request,
                UserDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getUserId());
        assertEquals("John", response.getBody().getName());
        assertEquals("Doe", response.getBody().getSurname());
        assertEquals("john@example.com", response.getBody().getEmail());

        assertTrue(userRepository.findByUserId(userId).isPresent());
    }

    @Test
    void testGetUserByEmailInternal() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setName("John");
        user.setSurname("Doe");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setEmail("john@example.com");
        user.setActive(true);
        userRepository.save(user);

        ResponseEntity<UserDto> response = restTemplate.exchange(
                baseUrl + "/internal/users/email/john@example.com",
                HttpMethod.GET,
                null,
                UserDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John", response.getBody().getName());
        assertEquals("john@example.com", response.getBody().getEmail());
    }

    @Test
    void testDeleteUserByUserIdInternal() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUserId(userId);
        user.setName("John");
        user.setSurname("Doe");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setEmail("john@example.com");
        user.setActive(true);
        user = userRepository.save(user);

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/internal/users/" + userId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(userRepository.findByUserId(userId).isPresent());
    }

    @Test
    void testRegistrationFlowSimulation() {
        UUID userId = UUID.randomUUID();

        UserCreateRequest createRequest = new UserCreateRequest();
        createRequest.setUserId(userId);
        createRequest.setName("John");
        createRequest.setSurname("Doe");
        createRequest.setBirthDate(LocalDate.of(1990, 1, 1));
        createRequest.setEmail("john@example.com");

        ResponseEntity<UserDto> createResponse = restTemplate.postForEntity(
                baseUrl + "/internal/users",
                createRequest,
                UserDto.class
        );

        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());

        ResponseEntity<UserDto> getResponse = restTemplate.exchange(
                baseUrl + "/internal/users/email/john@example.com",
                HttpMethod.GET,
                null,
                UserDto.class
        );

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals("John", getResponse.getBody().getName());

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                baseUrl + "/internal/users/" + userId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());
        assertFalse(userRepository.findByUserId(userId).isPresent());
    }

    @Test
    void testGetUserByEmailNotFound() {
        assertThrows(org.springframework.web.client.HttpClientErrorException.NotFound.class, () -> {
            restTemplate.exchange(
                    baseUrl + "/internal/users/email/nonexistent@example.com",
                    HttpMethod.GET,
                    null,
                    String.class
            );
        });
    }

    @Test
    void testDeleteUserByUserIdNotFound() {
        UUID userId = UUID.randomUUID();

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/internal/users/" + userId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}
