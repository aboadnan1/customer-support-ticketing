package com.aldaleel.ticketing.dto.request;

import com.aldaleel.ticketing.entity.User;

public record UpdateUserRequest(
        String name,
        String password,
        User.Role role
) {
}
