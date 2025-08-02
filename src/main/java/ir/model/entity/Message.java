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

@Entity(name="messageEntity")
@Table(name="message_tbl")
@Where(clause = "deleted = false")
public class Message extends Base {
    @Id
    @SequenceGenerator(name = "messageSeq", sequenceName = "message_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "messageSeq")
    @Column(name = "id")
    private Long id;
    private String content;

    @Column(name="date_time")
    private LocalDateTime dateTime;

    @ManyToOne
    @JoinColumn(name="ticket_id")
    private Ticket ticket;


    @ManyToOne
    @JoinColumn(name="username")
    private User user;
}
