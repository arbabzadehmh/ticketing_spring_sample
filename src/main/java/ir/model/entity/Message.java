package ir.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @OneToMany(mappedBy = "message", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Attachment> attachments = new ArrayList<>();

}
