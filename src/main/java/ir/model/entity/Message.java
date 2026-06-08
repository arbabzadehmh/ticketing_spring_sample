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
@Table(
        name = "message_tbl",
        indexes = {
                @Index(
                        name = "idx_message_ticket_datetime",
                        columnList = "ticket_id, date_time"
                )
        }
)
@Where(clause = "deleted = false")
public class Message extends Base {
    @Id
    @SequenceGenerator(name = "messageSeq", sequenceName = "message_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "messageSeq")
    @Column(name = "id")
    private Long id;

    @Column(name = "content", length = 300)
    private String content;

    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @Column(name = "sender_username", nullable = false)
    private String senderUsername;

    @Column(name = "sender_role_name", nullable = false)
    private String senderRoleName;

    @Column(name = "seen_by_admin")
    private boolean seenByAdmin = false;

    @Column(name = "seen_by_customer")
    private boolean seenByCustomer = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "ticket_id",
            foreignKey = @ForeignKey(name = "fk_message_ticket")
    )
    private Ticket ticket;

    @OneToMany(mappedBy = "message", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Attachment> attachments;

    public void addAttachment(Attachment attachment) {
        if (attachments == null) {
            attachments = new ArrayList<>();
        }
        attachments.add(attachment);
    }
}
