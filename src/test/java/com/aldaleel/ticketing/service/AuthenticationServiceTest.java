package com.aldaleel.ticketing.service;

import com.aldaleel.ticketing.entity.User;
import com.aldaleel.ticketing.repository.UserRepository;
import com.aldaleel.ticketing.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void login_shouldReturnJwtForValidCredentials() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("customer@example.com")
                .password("hashed-password")
                .role(User.Role.CUSTOMER)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(userId, user.getEmail(), "CUSTOMER"))
                .thenReturn("signed-token");

        var result = authenticationService.login(user.getEmail(), "password");

        assertEquals("signed-token", result.token());
        assertEquals(userId, result.userId());
        assertEquals("CUSTOMER", result.role());
    }

    @Test
    void login_shouldRejectInvalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        assertThrows(
                BadCredentialsException.class,
                () -> authenticationService.login("customer@example.com", "wrong-password")
        );
    }
}
