package com.aldaleel.ticketing.controller;

import com.aldaleel.ticketing.dto.request.CreateUserRequest;
import com.aldaleel.ticketing.dto.response.UserResponse;
import com.aldaleel.ticketing.entity.User;
import com.aldaleel.ticketing.mapper.UserMapper;
import com.aldaleel.ticketing.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .role(request.role())
                .build();

        User savedUser = userService.createUser(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserMapper.toResponse(savedUser));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        List<UserResponse> response = userService
                .getAllUsers()
                .stream()
                .map(UserMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable UUID id
    ) {

        User user = userService.getUserById(id);

        return ResponseEntity.ok(
                UserMapper.toResponse(user)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @RequestBody User user
    ) {

        User updatedUser = userService.updateUser(id, user);

        return ResponseEntity.ok(
                UserMapper.toResponse(updatedUser)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID id
    ) {

        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }
}