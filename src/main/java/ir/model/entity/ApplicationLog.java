package ir.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "application_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ApplicationLog {

    @Id
    @SequenceGenerator(name = "application_log_seq_gen", sequenceName = "application_log_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "application_log_seq_gen")
    private Long id;

    private String logLevel;

    private String logger;

    @Lob
    private String message;

    private String thread;

    private LocalDateTime createdAt;
}
