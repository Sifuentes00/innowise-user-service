package com.matvey.innowiseuserservice.service;

import com.matvey.innowiseuserservice.dto.PaymentCardDto;
import com.matvey.innowiseuserservice.entity.PaymentCard;
import com.matvey.innowiseuserservice.entity.User;
import com.matvey.innowiseuserservice.exception.BadRequestException;
import com.matvey.innowiseuserservice.exception.NotFoundException;
import com.matvey.innowiseuserservice.mapper.PaymentCardMapper;
import com.matvey.innowiseuserservice.repository.PaymentCardRepository;
import com.matvey.innowiseuserservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCardServiceTest {

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentCardMapper paymentCardMapper;

    @InjectMocks
    private PaymentCardService paymentCardService;

    private PaymentCard paymentCard;
    private PaymentCardDto paymentCardDto;
    private User user;
    private UUID cardId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        cardId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setName("John");
        user.setSurname("Doe");

        paymentCard = new PaymentCard();
        paymentCard.setId(cardId);
        paymentCard.setUser(user);
        paymentCard.setNumber("1234567890123456");
        paymentCard.setHolder("John Doe");
        paymentCard.setExpirationDate(LocalDate.of(2025, 12, 31));
        paymentCard.setActive(true);

        paymentCardDto = new PaymentCardDto();
        paymentCardDto.setId(cardId);
        paymentCardDto.setUserId(userId);
        paymentCardDto.setNumber("1234567890123456");
        paymentCardDto.setHolder("John Doe");
        paymentCardDto.setExpirationDate(LocalDate.of(2025, 12, 31));
        paymentCardDto.setActive(true);
    }

    @Test
    void testCreate_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(paymentCardRepository.findByUserId(userId)).thenReturn(new ArrayList<>());
        when(paymentCardMapper.toEntity(paymentCardDto)).thenReturn(paymentCard);
        when(paymentCardRepository.save(paymentCard)).thenReturn(paymentCard);
        when(paymentCardMapper.toDto(paymentCard)).thenReturn(paymentCardDto);

        PaymentCardDto result = paymentCardService.create(paymentCardDto);

        assertNotNull(result);
        assertEquals(paymentCardDto.getNumber(), result.getNumber());
        verify(userRepository, times(1)).findById(userId);
        verify(paymentCardRepository, times(1)).save(paymentCard);
    }

    @Test
    void testCreate_UserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> paymentCardService.create(paymentCardDto));
        verify(userRepository, times(1)).findById(userId);
        verify(paymentCardRepository, never()).save(any());
    }

    @Test
    void testCreate_MaxCardsExceeded() {
        List<PaymentCard> existingCards = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            existingCards.add(new PaymentCard());
        }

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(paymentCardRepository.findByUserId(userId)).thenReturn(existingCards);

        assertThrows(BadRequestException.class, () -> paymentCardService.create(paymentCardDto));
        verify(userRepository, times(1)).findById(userId);
        verify(paymentCardRepository, never()).save(any());
    }

    @Test
    void testGetById_Success() {
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(paymentCard));
        when(paymentCardMapper.toDto(paymentCard)).thenReturn(paymentCardDto);

        PaymentCardDto result = paymentCardService.getById(cardId);

        assertNotNull(result);
        assertEquals(cardId, result.getId());
        verify(paymentCardRepository, times(1)).findById(cardId);
    }

    @Test
    void testGetById_NotFound() {
        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> paymentCardService.getById(cardId));
        verify(paymentCardRepository, times(1)).findById(cardId);
    }

    @Test
    void testGetAll_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PaymentCard> cardPage = new PageImpl<>(List.of(paymentCard));

        when(paymentCardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(cardPage);
        when(paymentCardMapper.toDto(paymentCard)).thenReturn(paymentCardDto);

        Page<PaymentCardDto> result = paymentCardService.getAll(null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(paymentCardRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testGetAll_WithFilter_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<PaymentCard> cardPage = new PageImpl<>(List.of(paymentCard));

        when(paymentCardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(cardPage);
        when(paymentCardMapper.toDto(paymentCard)).thenReturn(paymentCardDto);

        Page<PaymentCardDto> result = paymentCardService.getAll("John Doe", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(paymentCardRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testGetByUserId_Success() {
        when(paymentCardRepository.findByUserId(userId)).thenReturn(List.of(paymentCard));
        when(paymentCardMapper.toDto(paymentCard)).thenReturn(paymentCardDto);

        List<PaymentCardDto> result = paymentCardService.getByUserId(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentCardRepository, times(1)).findByUserId(userId);
    }

    @Test
    void testUpdate_Success() {
        PaymentCardDto updatedDto = new PaymentCardDto();
        updatedDto.setNumber("9876543210987654");
        updatedDto.setHolder("Jane Smith");
        updatedDto.setExpirationDate(LocalDate.of(2026, 12, 31));

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.of(paymentCard));
        when(paymentCardRepository.save(paymentCard)).thenReturn(paymentCard);
        when(paymentCardMapper.toDto(paymentCard)).thenReturn(updatedDto);

        PaymentCardDto result = paymentCardService.update(cardId, updatedDto);

        assertNotNull(result);
        verify(paymentCardRepository, times(1)).findById(cardId);
        verify(paymentCardRepository, times(1)).save(paymentCard);
        verify(paymentCardMapper, times(1)).updateEntityFromDto(updatedDto, paymentCard);
    }

    @Test
    void testUpdate_NotFound() {
        PaymentCardDto updatedDto = new PaymentCardDto();

        when(paymentCardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> paymentCardService.update(cardId, updatedDto));
        verify(paymentCardRepository, times(1)).findById(cardId);
        verify(paymentCardRepository, never()).save(any());
    }

    @Test
    void testActivate_Success() {
        paymentCardService.activate(cardId);

        verify(paymentCardRepository, times(1)).updateActiveStatus(cardId, true);
    }

    @Test
    void testDeactivate_Success() {
        paymentCardService.deactivate(cardId);

        verify(paymentCardRepository, times(1)).updateActiveStatus(cardId, false);
    }

    @Test
    void testDelete_Success() {
        paymentCardService.delete(cardId);

        verify(paymentCardRepository, times(1)).deleteById(cardId);
    }
}
