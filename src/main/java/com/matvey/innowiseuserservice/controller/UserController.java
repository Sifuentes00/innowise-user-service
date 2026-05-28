package com.matvey.innowiseuserservice.controller;

import com.matvey.innowiseuserservice.dto.UserDto;
import com.matvey.innowiseuserservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        UserDto createdUser = userService.create(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getMyProfile() {
        UUID currentUserId = com.matvey.innowiseuserservice.util.SecurityUtils.getCurrentUserId();
        UserDto userDto = userService.getByUserId(currentUserId);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID userId) {
        UserDto userDto = userService.getByUserId(userId);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<UserDto> users = userService.getAll(name, surname, pageable);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal")
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID userId, @Valid @RequestBody UserDto userDto) {
        UserDto existingUser = userService.getByUserId(userId);
        UserDto updatedUser = userService.update(existingUser.getId(), userDto);
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateUser(@PathVariable UUID userId) {
        UserDto user = userService.getByUserId(userId);
        userService.activate(user.getId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID userId) {
        UserDto user = userService.getByUserId(userId);
        userService.deactivate(user.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        UserDto user = userService.getByUserId(userId);
        userService.delete(user.getId());
        return ResponseEntity.noContent().build();
    }
}
