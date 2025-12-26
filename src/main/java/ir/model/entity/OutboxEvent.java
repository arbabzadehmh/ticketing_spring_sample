package ir.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Where;

import java.time.Instant;


@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@ToString

@Entity(name = "outboxEntity")
@Table(name = "outbox_tbl")
@Where(clause = "deleted = false")
public class OutboxEvent extends Base{

    @Id
    @SequenceGenerator(name = "outboxSeq", sequenceName = "outbox_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "outboxSeq")
    @Column(name = "id")
    private Long id;

    @Column(name = "aggregate_type")
    private String aggregateType; // e.g., "BUILDING"

    @Column(name = "aggregate_id")
    private Long aggregateId;

    @Column(name = "event_type")
    private String eventType; // e.g., "ADDRESS_CREATE_REQUEST"

    @Lob
    @Column(name = "payload")
    private String payload; // JSON

    @Column(name = "published")
    private boolean published = false;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
}
