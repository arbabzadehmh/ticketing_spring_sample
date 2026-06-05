package ir.service.impl;

import ir.repository.ProfileRepository;
import ir.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetService resetService;
    private final EmailService emailService;
    private final ProfileRepository profileRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    public AuthService(UserRepository userRepository, PasswordResetService resetService, EmailService emailService, ProfileRepository profileRepository) {
        this.userRepository = userRepository;
        this.resetService = resetService;
        this.emailService = emailService;
        this.profileRepository = profileRepository;
    }

    public void requestReset(String username) {

        userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));


        String token = resetService.createToken(username);


        String link = baseUrl + "/profiles/reset-password?token=" + token;

        emailService.sendEmail(
                profileRepository.findEmailByUserUsername(username),
                "Password Reset",
                "Click the link to reset your password:\n" + link
        );
    }
}
