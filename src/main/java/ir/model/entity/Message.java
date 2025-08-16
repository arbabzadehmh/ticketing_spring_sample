package ir.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@ToString

@Entity(name = "messageEntity")
@Table(name = "message_tbl")
@Where(clause = "deleted = false")
public class Message extends Base {
    @Id
    @SequenceGenerator(name = "messageSeq", sequenceName = "message_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "messageSeq")
    @Column(name = "id")
    private Long id;

    @Column(name = "content", length = 300, nullable = false)
    private String content;

    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @Column(name = "sender_username", nullable = false)
    private String senderUsername;

    @Column(name = "sender_role_name", nullable = false)
    private String senderRoleName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", foreignKey = @ForeignKey(name = "fk_message_ticket"))
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "username",
            foreignKey = @ForeignKey(name = "fk_message_user"),
            updatable = false
    )
    private User user;
}
