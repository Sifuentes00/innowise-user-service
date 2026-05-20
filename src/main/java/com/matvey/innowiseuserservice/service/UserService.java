package com.matvey.innowiseuserservice.service;

import com.matvey.innowiseuserservice.dto.UserDto;
import com.matvey.innowiseuserservice.entity.User;
import com.matvey.innowiseuserservice.exception.NotFoundException;
import com.matvey.innowiseuserservice.mapper.UserMapper;
import com.matvey.innowiseuserservice.repository.UserRepository;
import com.matvey.innowiseuserservice.specification.UserSpecification;
import org.springframework.beans.factory.annotation.Autowired;
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

    public UserDto getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        return userMapper.toDto(user);
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
    public UserDto update(UUID id, UserDto userDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        userMapper.updateEntityFromDto(userDto, existingUser);
        User updatedUser = userRepository.save(existingUser);
        return userMapper.toDto(updatedUser);
    }

    @Transactional
    public void activate(UUID id) {
        userRepository.updateActiveStatus(id, true);
    }

    @Transactional
    public void deactivate(UUID id) {
        userRepository.updateActiveStatus(id, false);
    }

    @Transactional
    public void delete(UUID id) {
        userRepository.deleteById(id);
    }
}
