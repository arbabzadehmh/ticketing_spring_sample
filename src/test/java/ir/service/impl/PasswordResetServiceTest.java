package ir.service.impl;

import ir.controller.exception.ExpiredPasswordTokenException;
import ir.controller.exception.InvalidPasswordTokenException;
import ir.controller.exception.UsedPasswordTokenException;
import ir.model.entity.PasswordResetToken;
import ir.repository.PasswordResetTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @InjectMocks
    private PasswordResetService passwordResetService;


    @Test
    void createToken_shouldDeleteOldTokensAndSaveNewToken() {

        String username = "admin";

        String token =
                passwordResetService.createToken(username);


        assertNotNull(token);


        verify(tokenRepository)
                .deleteByUsername(username);


        ArgumentCaptor<PasswordResetToken> captor =
                ArgumentCaptor.forClass(
                        PasswordResetToken.class
                );


        verify(tokenRepository)
                .save(captor.capture());


        PasswordResetToken savedToken =
                captor.getValue();


        assertEquals(username, savedToken.getUsername());

        assertEquals(token, savedToken.getToken());

        assertFalse(savedToken.isUsed());

        assertTrue(
                savedToken.getExpiryDate()
                        .isAfter(LocalDateTime.now())
        );
    }


    @Test
    void validateToken_shouldReturnTokenWhenValid() {

        PasswordResetToken token =
                PasswordResetToken.builder()
                        .token("abc123")
                        .username("admin")
                        .used(false)
                        .expiryDate(
                                LocalDateTime.now().plusMinutes(10)
                        )
                        .build();


        when(tokenRepository.findByToken("abc123"))
                .thenReturn(Optional.of(token));


        PasswordResetToken result =
                passwordResetService.validateToken("abc123");


        assertEquals(token, result);
    }


    @Test
    void validateToken_shouldThrowInvalidExceptionWhenNotFound() {

        when(tokenRepository.findByToken("wrong"))
                .thenReturn(Optional.empty());


        assertThrows(
                InvalidPasswordTokenException.class,
                () -> passwordResetService.validateToken("wrong")
        );
    }


    @Test
    void validateToken_shouldThrowUsedException() {

        PasswordResetToken token =
                PasswordResetToken.builder()
                        .token("abc")
                        .used(true)
                        .expiryDate(
                                LocalDateTime.now().plusMinutes(10)
                        )
                        .build();


        when(tokenRepository.findByToken("abc"))
                .thenReturn(Optional.of(token));


        assertThrows(
                UsedPasswordTokenException.class,
                () -> passwordResetService.validateToken("abc")
        );
    }


    @Test
    void validateToken_shouldThrowExpiredException() {

        PasswordResetToken token =
                PasswordResetToken.builder()
                        .token("abc")
                        .used(false)
                        .expiryDate(
                                LocalDateTime.now().minusMinutes(1)
                        )
                        .build();


        when(tokenRepository.findByToken("abc"))
                .thenReturn(Optional.of(token));


        assertThrows(
                ExpiredPasswordTokenException.class,
                () -> passwordResetService.validateToken("abc")
        );
    }


    @Test
    void markAsUsed_shouldUpdateTokenAndSave() {

        PasswordResetToken token =
                new PasswordResetToken();

        token.setUsed(false);


        passwordResetService.markAsUsed(token);


        assertTrue(token.isUsed());


        verify(tokenRepository)
                .save(token);
    }
}
