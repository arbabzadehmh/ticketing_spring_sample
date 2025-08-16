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
import java.util.ArrayList;
import java.util.List;

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

    @ManyToOne
    @JoinColumn(name = "section_id", foreignKey = @ForeignKey(name = "fk_ticket_section"))
    private Section section;

    @OneToMany(mappedBy = "ticket", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Message> messageList;

    @ManyToOne
    @JoinColumn(name="customer", foreignKey = @ForeignKey(name = "fk_ticket_user"))
    private User customer;

    public void addMessage(Message message) {
        if(messageList == null) {
            messageList = new ArrayList<>();
        }
        messageList.add(message);
    }
}
