package com.matvey.innowiseuserservice.service;

import com.matvey.innowiseuserservice.dto.UserCreateRequest;
import com.matvey.innowiseuserservice.dto.UserDto;
import com.matvey.innowiseuserservice.entity.User;
import com.matvey.innowiseuserservice.exception.NotFoundException;
import com.matvey.innowiseuserservice.mapper.UserMapper;
import com.matvey.innowiseuserservice.repository.UserRepository;
import com.matvey.innowiseuserservice.specification.UserSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private com.matvey.innowiseuserservice.mapper.PaymentCardMapper paymentCardMapper;

    public UserDto create(UserDto userDto) {
        if (userDto == null) {
            throw new IllegalArgumentException("UserDto cannot be null");
        }
        User user = userMapper.toEntity(userDto);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    public UserDto createUser(UserCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("UserCreateRequest cannot be null");
        }
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        User user = new User();
        user.setUserId(request.getUserId());
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setBirthDate(request.getBirthDate());
        user.setEmail(request.getEmail());
        user.setActive(true);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Cacheable(value = "users", key = "#id")
    public UserDto getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        user.getPaymentCards().size();
        UserDto userDto = userMapper.toDto(user);
        userDto.setPaymentCards(user.getPaymentCards().stream()
                .map(paymentCardMapper::toDto)
                .collect(java.util.stream.Collectors.toList()));
        return userDto;
    }

    @Cacheable(value = "users", key = "#userId")
    public UserDto getByUserId(UUID userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("User not found with userId: " + userId));
        user.getPaymentCards().size();
        UserDto userDto = userMapper.toDto(user);
        userDto.setPaymentCards(user.getPaymentCards().stream()
                .map(paymentCardMapper::toDto)
                .collect(java.util.stream.Collectors.toList()));
        return userDto;
    }

    public Page<UserDto> getAll(String name, String surname, Pageable pageable) {
        Specification<User> spec = Specification.where((root, query, cb) -> cb.conjunction());

        if (name != null || surname != null) {
            spec = spec.and(UserSpecification.byNameAndSurname(name, surname));
        }

        Page<User> userPage = userRepository.findAll(spec, pageable);
        return userPage.map(userMapper::toDto);
    }

    @Transactional
    @CachePut(value = "users", key = "#id")
    public UserDto update(UUID id, UserDto userDto) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        if (userDto == null) {
            throw new IllegalArgumentException("UserDto cannot be null");
        }
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        userMapper.updateEntityFromDto(userDto, existingUser);
        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void activate(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        user.setActive(true);
        userRepository.save(user);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deactivate(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        user.setActive(false);
        userRepository.save(user);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void delete(UUID id) {
        userRepository.deleteById(id);
    }
}
