package ir.service.impl;

import ir.model.entity.PasswordResetToken;
import ir.repository.PasswordResetTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Transactional
    public String createToken(String username) {

        // حذف توکن‌های قبلی (اختیاری ولی بهتر)
        tokenRepository.deleteByUsername(username);

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken =
                PasswordResetToken
                        .builder()
                        .token(token)
                        .username(username)
                        .expiryDate(LocalDateTime.now().plusMinutes(15))
                        .used(false)
                        .build();

        tokenRepository.save(resetToken);

        return token;
    }

    public PasswordResetToken validateToken(String token) {

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.isUsed()) {
            throw new RuntimeException("Token already used");
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        return resetToken;
    }

    public void markAsUsed(PasswordResetToken token) {
        token.setUsed(true);
        tokenRepository.save(token);
    }
}
