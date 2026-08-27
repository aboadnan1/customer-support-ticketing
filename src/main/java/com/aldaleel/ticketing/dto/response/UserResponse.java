package com.aldaleel.ticketing.dto.response;

import com.aldaleel.ticketing.entity.User;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        User.Role role
) {
}