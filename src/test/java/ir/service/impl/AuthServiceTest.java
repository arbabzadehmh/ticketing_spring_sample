package ir.service.impl;

import ir.model.entity.User;
import ir.repository.ProfileRepository;
import ir.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetService resetService;

    @Mock
    private EmailService emailService;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void init() throws Exception {
        Field field =
                AuthService.class.getDeclaredField("baseUrl");
        field.setAccessible(true);
        field.set(authService, "http://localhost:8080");
    }

    @Test
    void requestReset_shouldSendEmail() {

        User user = new User();

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(user));

        when(resetService.createToken("admin"))
                .thenReturn("TOKEN123");

        when(profileRepository.findEmailByUserUsername("admin"))
                .thenReturn("admin@test.com");

        authService.requestReset("admin");

        verify(emailService).sendEmail(
                eq("admin@test.com"),
                eq("Password Reset"),
                contains("TOKEN123")
        );
    }

    @Test
    void requestReset_shouldThrowWhenUserNotFound() {

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> authService.requestReset("admin")
        );
    }
}
