package ir.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;


    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(
                emailService,
                "fromEmail",
                "test@example.com"
        );
    }


    @Test
    void sendEmail_shouldSendMessageSuccessfully() {

        emailService.sendEmail(
                "user@test.com",
                "Hello",
                "Message"
        );

        verify(mailSender)
                .send(any(SimpleMailMessage.class));
    }


    @Test
    void sendEmail_shouldHandleExceptionWhenSendingFails() {

        doThrow(new MailException("SMTP error") {})
                .when(mailSender)
                .send(any(SimpleMailMessage.class));


        assertDoesNotThrow(() ->
                emailService.sendEmail(
                        "user@test.com",
                        "Hello",
                        "Message"
                )
        );

        verify(mailSender)
                .send(any(SimpleMailMessage.class));
    }

}
