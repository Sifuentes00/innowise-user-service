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

    public UserDto create(UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    public UserDto createUser(UserCreateRequest request) {
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
        return userMapper.toDto(user);
    }

    @Cacheable(value = "users", key = "#userId")
    public UserDto getByUserId(UUID userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("User not found with userId: " + userId));
        return userMapper.toDto(user);
    }

    @Cacheable(value = "users")
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
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        userMapper.updateEntityFromDto(userDto, existingUser);
        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void activate(UUID id) {
        userRepository.updateActiveStatus(id, true);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deactivate(UUID id) {
        userRepository.updateActiveStatus(id, false);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void delete(UUID id) {
        userRepository.deleteById(id);
    }
}
