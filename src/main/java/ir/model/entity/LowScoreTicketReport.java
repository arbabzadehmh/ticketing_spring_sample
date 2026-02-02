package ir.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@ToString

@Entity(name="lowScoreTicketReportEntity")
@Table(name="low_score_ticket_report_table")
public class LowScoreTicketReport {

    @Id
    @SequenceGenerator(name = "reportSeq",sequenceName = "report_seq" , allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "reportSeq")
    @Column(name="id")
    private Long id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "from_date", nullable = false)
    private LocalDateTime fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDateTime toDate;

    @ElementCollection
    @CollectionTable(
            name = "low_score_ticket_report_items",
            joinColumns = @JoinColumn(name = "report_id")
    )
    @Column(name = "ticket_id")
    private List<Long> ticketIds;
}
