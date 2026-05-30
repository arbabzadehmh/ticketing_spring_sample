package ir.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@ToString

@Entity(name = "passwordResetTokenEntity")
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @SequenceGenerator(name = "passwordResetTokenSeq",sequenceName = "password_reset_tokens_seq" , allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "passwordResetTokenSeq")
    @Column(name="id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    private boolean used = false;
}
