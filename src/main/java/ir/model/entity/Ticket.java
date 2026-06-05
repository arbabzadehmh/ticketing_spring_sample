package ir.model.entity;

import ir.model.enums.TicketStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

@Entity(name="ticketEntity")
@Table(name="ticket_tbl")
@Where(clause = "deleted = false")
public class Ticket extends Base{
    @Id
    @SequenceGenerator(name = "ticketSeq", sequenceName = "ticket_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ticketSeq")
    @Column(name = "id")
    private Long id;

    @Column(name = "title", length = 100)
    @NotBlank(message = "{validation.title}")
    @Size(max = 100, message = "{validation.titleSize}")
    private String title;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status", columnDefinition = "smallint")
    private TicketStatus status;

    @Column(name="date_time")
    private LocalDateTime dateTime;

    @Column(name = "score")
    private Integer score;

    @Column(name = "admin_unread")
    private boolean adminUnread;

    @Column(name = "customer_unread")
    private boolean customerUnread;

    @Column(name = "section_id")
    private Long sectionId;

    @Column(name = "section_title")
    private String sectionTitle;

    @ManyToOne
    @JoinColumn(name="customer", foreignKey = @ForeignKey(name = "fk_ticket_user"))
    private User customer;

}
