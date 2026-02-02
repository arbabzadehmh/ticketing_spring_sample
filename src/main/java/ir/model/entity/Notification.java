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

@Entity(name="notificationEntity")
@Table(name="notification_table")
public class Notification {

    @Id
    @SequenceGenerator(name = "notificationSeq",sequenceName = "notification_seq" , allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "notificationSeq")
    @Column(name="id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "message")
    private String message;

    @Column(name = "read")
    private boolean read = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "link")
    private String link;

    @ManyToOne
    @JoinColumn(name="user", foreignKey = @ForeignKey(name = "fk_notification_user"))
    private User user;
}
