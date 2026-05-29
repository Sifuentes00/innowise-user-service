package com.matvey.innowiseuserservice.controller;

import com.matvey.innowiseuserservice.dto.UserCreateRequest;
import com.matvey.innowiseuserservice.dto.UserDto;
import com.matvey.innowiseuserservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserDto result = userService.createUser(request);
        return ResponseEntity.ok(result);
    }
}
