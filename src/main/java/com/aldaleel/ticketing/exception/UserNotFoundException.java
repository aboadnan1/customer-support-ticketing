package com.aldaleel.ticketing.exception;

import java.util.UUID;

public class UserNotFoundException extends ApplicationException {

    public UserNotFoundException(UUID userId) {
        super("User not found: " + userId);
    }
}
