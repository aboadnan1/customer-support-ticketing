package com.aldaleel.ticketing.mapper;

import com.aldaleel.ticketing.dto.response.UserResponse;
import com.aldaleel.ticketing.entity.User;

public class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}