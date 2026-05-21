package com.matvey.innowiseuserservice.integration;

import com.matvey.innowiseuserservice.dto.PaymentCardDto;
import com.matvey.innowiseuserservice.entity.PaymentCard;
import com.matvey.innowiseuserservice.entity.User;
import com.matvey.innowiseuserservice.repository.PaymentCardRepository;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@org.springframework.test.context.ActiveProfiles("test")
class PaymentCardIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16-alpine"))
            .withReuse(true);

    @LocalServerPort
    private int port;

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @Autowired
    private UserRepository userRepository;

    private RestTemplate restTemplate;
    private String baseUrl;
    private User user;

    @BeforeEach
    void setUp() {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        restTemplate = new RestTemplate(factory);
        baseUrl = "http://localhost:" + port;
        paymentCardRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setName("John");
        user.setSurname("Doe");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setEmail("john@example.com");
        user.setActive(true);
        user = userRepository.save(user);
    }

    @Test
    void testCreatePaymentCard() {
        PaymentCardDto paymentCardDto = new PaymentCardDto();
        paymentCardDto.setUserId(user.getId());
        paymentCardDto.setNumber("1234567890123456");
        paymentCardDto.setHolder("John Doe");
        paymentCardDto.setExpirationDate(LocalDate.of(2030, 12, 31));
        paymentCardDto.setActive(true);

        ResponseEntity<PaymentCardDto> response = restTemplate.postForEntity(
                baseUrl + "/api/payment-cards",
                paymentCardDto,
                PaymentCardDto.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("1234567890123456", response.getBody().getNumber());
        assertEquals("John Doe", response.getBody().getHolder());
    }

    @Test
    void testGetPaymentCardById() {
        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setUser(user);
        paymentCard.setNumber("1234567890123456");
        paymentCard.setHolder("John Doe");
        paymentCard.setExpirationDate(LocalDate.of(2030, 12, 31));
        paymentCard.setActive(true);
        paymentCard = paymentCardRepository.save(paymentCard);

        ResponseEntity<PaymentCardDto> response = restTemplate.getForEntity(
                baseUrl + "/api/payment-cards/" + paymentCard.getId(),
                PaymentCardDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("1234567890123456", response.getBody().getNumber());
        assertEquals("John Doe", response.getBody().getHolder());
    }

    @Test
    void testGetCardsByUserId() {
        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setUser(user);
        paymentCard.setNumber("1234567890123456");
        paymentCard.setHolder("John Doe");
        paymentCard.setExpirationDate(LocalDate.of(2030, 12, 31));
        paymentCard.setActive(true);
        paymentCardRepository.save(paymentCard);

        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/payment-cards/user/" + user.getId(),
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testGetAllPaymentCards() {
        PaymentCard paymentCard1 = new PaymentCard();
        paymentCard1.setUser(user);
        paymentCard1.setNumber("1234567890123456");
        paymentCard1.setHolder("John Doe");
        paymentCard1.setExpirationDate(LocalDate.of(2030, 12, 31));
        paymentCard1.setActive(true);
        paymentCardRepository.save(paymentCard1);

        PaymentCard paymentCard2 = new PaymentCard();
        paymentCard2.setUser(user);
        paymentCard2.setNumber("9876543210987654");
        paymentCard2.setHolder("Jane Smith");
        paymentCard2.setExpirationDate(LocalDate.of(2033, 6, 30));
        paymentCard2.setActive(true);
        paymentCardRepository.save(paymentCard2);

        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/payment-cards",
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testUpdatePaymentCard() {
        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setUser(user);
        paymentCard.setNumber("1234567890123456");
        paymentCard.setHolder("John Doe");
        paymentCard.setExpirationDate(LocalDate.of(2030, 12, 31));
        paymentCard.setActive(true);
        paymentCard = paymentCardRepository.save(paymentCard);

        ResponseEntity<PaymentCardDto> getResponse = restTemplate.getForEntity(
                baseUrl + "/api/payment-cards/" + paymentCard.getId(),
                PaymentCardDto.class
        );
        PaymentCardDto paymentCardDto = getResponse.getBody();
        paymentCardDto.setUserId(user.getId());
        paymentCardDto.setNumber("9876543210987654");
        paymentCardDto.setHolder("Jane Smith");
        paymentCardDto.setExpirationDate(LocalDate.of(2033, 6, 30));

        HttpEntity<PaymentCardDto> requestEntity = new HttpEntity<>(paymentCardDto);
        ResponseEntity<PaymentCardDto> response = restTemplate.exchange(
                baseUrl + "/api/payment-cards/" + paymentCard.getId(),
                HttpMethod.PUT,
                requestEntity,
                PaymentCardDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("9876543210987654", response.getBody().getNumber());
        assertEquals("Jane Smith", response.getBody().getHolder());
    }

    @Test
    void testDeletePaymentCard() {
        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setUser(user);
        paymentCard.setNumber("1234567890123456");
        paymentCard.setHolder("John Doe");
        paymentCard.setExpirationDate(LocalDate.of(2030, 12, 31));
        paymentCard.setActive(true);
        paymentCard = paymentCardRepository.save(paymentCard);

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/api/payment-cards/" + paymentCard.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(paymentCardRepository.existsById(paymentCard.getId()));
    }

    @Test
    void testActivatePaymentCard() {
        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setUser(user);
        paymentCard.setNumber("1234567890123456");
        paymentCard.setHolder("John Doe");
        paymentCard.setExpirationDate(LocalDate.of(2030, 12, 31));
        paymentCard.setActive(false);
        paymentCard = paymentCardRepository.save(paymentCard);

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/api/payment-cards/" + paymentCard.getId() + "/activate",
                HttpMethod.PATCH,
                null,
                Void.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        PaymentCard updatedCard = paymentCardRepository.findById(paymentCard.getId()).orElseThrow();
        assertTrue(updatedCard.getActive());
    }

    @Test
    void testDeactivatePaymentCard() {
        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setUser(user);
        paymentCard.setNumber("1234567890123456");
        paymentCard.setHolder("John Doe");
        paymentCard.setExpirationDate(LocalDate.of(2030, 12, 31));
        paymentCard.setActive(true);
        paymentCard = paymentCardRepository.save(paymentCard);

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/api/payment-cards/" + paymentCard.getId() + "/deactivate",
                HttpMethod.PATCH,
                null,
                Void.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        PaymentCard updatedCard = paymentCardRepository.findById(paymentCard.getId()).orElseThrow();
        assertFalse(updatedCard.getActive());
    }

    @Test
    void testCascadeDelete() {
        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setUser(user);
        paymentCard.setNumber("1234567890123456");
        paymentCard.setHolder("John Doe");
        paymentCard.setExpirationDate(LocalDate.of(2030, 12, 31));
        paymentCard.setActive(true);
        paymentCard = paymentCardRepository.save(paymentCard);

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/api/users/" + user.getId(),
                HttpMethod.DELETE,
                null,
                Void.class
        );

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertEquals(0, paymentCardRepository.findAll().size());
    }
}
