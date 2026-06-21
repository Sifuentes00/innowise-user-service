package com.matvey.innowiseuserservice.integration;

import com.matvey.innowiseuserservice.dto.UserDto;
import com.matvey.innowiseuserservice.entity.User;
import com.matvey.innowiseuserservice.repository.UserRepository;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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
class UserIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16-alpine"))
            .withReuse(true);

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }

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

    private HttpHeaders createGatewayHeaders(UUID userId, String role) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        headers.set("X-User-Role", role);
        return headers;
    }

    @Test
    void testCreateUser() {
        UserDto userDto = new UserDto();
        userDto.setName("John");
        userDto.setSurname("Doe");
        userDto.setBirthDate(LocalDate.of(1990, 1, 1));
        userDto.setEmail("john@example.com");
        userDto.setActive(true);

        UUID userId = UUID.randomUUID();
        HttpHeaders headers = createGatewayHeaders(userId, "ADMIN");
        HttpEntity<UserDto> request = new HttpEntity<>(userDto, headers);

        ResponseEntity<UserDto> response = restTemplate.postForEntity(
                baseUrl + "/api/users",
                request,
                UserDto.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("John", response.getBody().getName());
        assertEquals("Doe", response.getBody().getSurname());
        assertEquals("john@example.com", response.getBody().getEmail());
    }

    @Test
    void testGetUserById() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUserId(userId);
        user.setName("John");
        user.setSurname("Doe");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setEmail("john@example.com");
        user.setActive(true);
        user = userRepository.save(user);

        HttpHeaders headers = createGatewayHeaders(userId, "ADMIN");
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<UserDto> response = restTemplate.exchange(
                baseUrl + "/api/users/" + user.getUserId(),
                HttpMethod.GET,
                request,
                UserDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John", response.getBody().getName());
        assertEquals("Doe", response.getBody().getSurname());
    }

    @Test
    void testGetAllUsers() {
        User user1 = new User();
        user1.setName("John");
        user1.setSurname("Doe");
        user1.setBirthDate(LocalDate.of(1990, 1, 1));
        user1.setEmail("john@example.com");
        user1.setActive(true);
        userRepository.save(user1);

        User user2 = new User();
        user2.setName("Jane");
        user2.setSurname("Smith");
        user2.setBirthDate(LocalDate.of(1995, 5, 15));
        user2.setEmail("jane@example.com");
        user2.setActive(true);
        userRepository.save(user2);

        UUID userId = UUID.randomUUID();
        HttpHeaders headers = createGatewayHeaders(userId, "ADMIN");
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/users",
                HttpMethod.GET,
                request,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testUpdateUser() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUserId(userId);
        user.setName("John");
        user.setSurname("Doe");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setEmail("john@example.com");
        user.setActive(true);
        user = userRepository.save(user);

        HttpHeaders headers = createGatewayHeaders(userId, "ADMIN");

        ResponseEntity<UserDto> getResponse = restTemplate.exchange(
                baseUrl + "/api/users/" + user.getUserId(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                UserDto.class
        );
        UserDto userDto = getResponse.getBody();
        userDto.setName("Jane");
        userDto.setSurname("Smith");
        userDto.setBirthDate(LocalDate.of(1995, 5, 15));
        userDto.setEmail("jane@example.com");

        HttpEntity<UserDto> requestEntity = new HttpEntity<>(userDto, headers);
        ResponseEntity<UserDto> response = restTemplate.exchange(
                baseUrl + "/api/users/" + user.getUserId(),
                HttpMethod.PUT,
                requestEntity,
                UserDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Jane", response.getBody().getName());
        assertEquals("Smith", response.getBody().getSurname());
    }

    @Test
    void testDeleteUser() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUserId(userId);
        user.setName("John");
        user.setSurname("Doe");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setEmail("john@example.com");
        user.setActive(true);
        user = userRepository.save(user);

        HttpHeaders headers = createGatewayHeaders(userId, "ADMIN");

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/api/users/" + user.getUserId(),
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(userRepository.existsById(user.getId()));
    }

    @Test
    void testActivateUser() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUserId(userId);
        user.setName("John");
        user.setSurname("Doe");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setEmail("john@example.com");
        user.setActive(false);
        user = userRepository.save(user);

        HttpHeaders headers = createGatewayHeaders(userId, "ADMIN");

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/api/users/" + user.getUserId() + "/activate",
                HttpMethod.PATCH,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertTrue(updatedUser.getActive());
    }

    @Test
    void testDeactivateUser() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUserId(userId);
        user.setName("John");
        user.setSurname("Doe");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setEmail("john@example.com");
        user.setActive(true);
        user = userRepository.save(user);

        HttpHeaders headers = createGatewayHeaders(userId, "ADMIN");

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/api/users/" + user.getUserId() + "/deactivate",
                HttpMethod.PATCH,
                new HttpEntity<>(headers),
                Void.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertFalse(updatedUser.getActive());
    }
}
