package ir.repository;

import ir.model.entity.PasswordResetToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordResetTokenRepositoryTest extends BaseRepositoryTest{

    @Autowired
    private PasswordResetTokenRepository repository;

    @Test
    void shouldFindByToken() {

        PasswordResetToken token =
                PasswordResetToken.builder()
                        .token("abc123")
                        .username("ali")
                        .expiryDate(LocalDateTime.now().plusMinutes(15))
                        .build();

        repository.save(token);

        var result =
                repository.findByToken("abc123");

        assertTrue(result.isPresent());

        assertEquals(
                "ali",
                result.get().getUsername()
        );
    }

    @Test
    void shouldDeleteByUsername() {

        PasswordResetToken token =
                PasswordResetToken.builder()
                        .token("abc123")
                        .username("ali")
                        .expiryDate(LocalDateTime.now().plusMinutes(15))
                        .build();

        repository.save(token);

        repository.deleteByUsername("ali");

        assertTrue(
                repository.findByToken("abc123")
                        .isEmpty()
        );
    }
}
