package com.matvey.innowiseuserservice.service;

import com.matvey.innowiseuserservice.dto.UserDto;
import com.matvey.innowiseuserservice.entity.User;
import com.matvey.innowiseuserservice.exception.NotFoundException;
import com.matvey.innowiseuserservice.mapper.UserMapper;
import com.matvey.innowiseuserservice.mapper.PaymentCardMapper;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PaymentCardMapper paymentCardMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserDto userDto;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setName("John");
        user.setSurname("Doe");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setEmail("john@example.com");
        user.setActive(true);

        userDto = new UserDto();
        userDto.setId(userId);
        userDto.setName("John");
        userDto.setSurname("Doe");
        userDto.setBirthDate(LocalDate.of(1990, 1, 1));
        userDto.setEmail("john@example.com");
        userDto.setActive(true);
    }

    @Test
    void testCreate_Success() {
        when(userMapper.toEntity(userDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = userService.create(userDto);

        assertNotNull(result);
        assertEquals(userDto.getName(), result.getName());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testGetById_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = userService.getById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testGetById_NotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getById(userId));
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testGetAll_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(java.util.List.of(user));

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(userMapper.toDto(user)).thenReturn(userDto);

        Page<UserDto> result = userService.getAll(null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testGetAll_WithFilters_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(java.util.List.of(user));

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(userMapper.toDto(user)).thenReturn(userDto);

        Page<UserDto> result = userService.getAll("John", "Doe", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testUpdate_Success() {
        UserDto updatedDto = new UserDto();
        updatedDto.setName("Jane");
        updatedDto.setSurname("Smith");
        updatedDto.setBirthDate(LocalDate.of(1995, 1, 1));
        updatedDto.setEmail("jane@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(updatedDto);

        UserDto result = userService.update(userId, updatedDto);

        assertNotNull(result);
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(user);
        verify(userMapper, times(1)).updateEntityFromDto(updatedDto, user);
    }

    @Test
    void testUpdate_NotFound() {
        UserDto updatedDto = new UserDto();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.update(userId, updatedDto));
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void testActivate_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        userService.activate(userId);

        assertTrue(user.getActive());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testDeactivate_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        userService.deactivate(userId);

        assertFalse(user.getActive());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testDelete_Success() {
        userService.delete(userId);

        verify(userRepository, times(1)).deleteById(userId);
    }
}
