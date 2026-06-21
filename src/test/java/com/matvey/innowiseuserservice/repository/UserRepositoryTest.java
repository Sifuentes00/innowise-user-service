package com.matvey.innowiseuserservice.repository;

import com.matvey.innowiseuserservice.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@org.springframework.test.context.ActiveProfiles("test")
class UserRepositoryTest {

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

    @Autowired
    private UserRepository userRepository;

    private User user;
    private UUID userId;
    private UUID businessUserId;

    @Test
    @Transactional
    @Rollback
    void testFindByEmail_Success() {
        businessUserId = UUID.randomUUID();

        user = new User();
        user.setUserId(businessUserId);
        user.setName("John" + UUID.randomUUID().toString().substring(0, 5));
        user.setSurname("Doe" + UUID.randomUUID().toString().substring(0, 5));
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setEmail("john" + UUID.randomUUID().toString().substring(0, 5) + "@example.com");
        user.setActive(true);
        user = userRepository.save(user);

        Optional<User> result = userRepository.findByEmail(user.getEmail());

        assertTrue(result.isPresent());
        assertEquals(user.getEmail(), result.get().getEmail());
    }

    @Test
    @Transactional
    @Rollback
    void testFindByEmail_NotFound() {
        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");

        assertFalse(result.isPresent());
    }

    @Test
    @Transactional
    @Rollback
    void testFindByUserId_Success() {
        businessUserId = UUID.randomUUID();

        user = new User();
        user.setUserId(businessUserId);
        user.setName("John" + UUID.randomUUID().toString().substring(0, 5));
        user.setSurname("Doe" + UUID.randomUUID().toString().substring(0, 5));
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setEmail("john" + UUID.randomUUID().toString().substring(0, 5) + "@example.com");
        user.setActive(true);
        user = userRepository.save(user);

        Optional<User> result = userRepository.findByUserId(businessUserId);

        assertTrue(result.isPresent());
        assertEquals(businessUserId, result.get().getUserId());
    }

    @Test
    @Transactional
    @Rollback
    void testFindByUserId_NotFound() {
        Optional<User> result = userRepository.findByUserId(UUID.randomUUID());

        assertFalse(result.isPresent());
    }

    @Test
    @Transactional
    @Rollback
    void testDeleteByUserId_Success() {
        businessUserId = UUID.randomUUID();

        user = new User();
        user.setUserId(businessUserId);
        user.setName("John" + UUID.randomUUID().toString().substring(0, 5));
        user.setSurname("Doe" + UUID.randomUUID().toString().substring(0, 5));
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setEmail("john" + UUID.randomUUID().toString().substring(0, 5) + "@example.com");
        user.setActive(true);
        user = userRepository.save(user);

        userRepository.deleteByUserId(businessUserId);

        Optional<User> result = userRepository.findByUserId(businessUserId);
        assertFalse(result.isPresent());
    }

    @Test
    @Transactional
    @Rollback
    void testDeleteByUserId_NonExistent() {
        UUID nonExistentUserId = UUID.randomUUID();

        assertDoesNotThrow(() -> userRepository.deleteByUserId(nonExistentUserId));
    }
}
