package ir.model.entity;



import ir.model.enums.FileType;
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

@Entity(name="attachmentEntity")
@Table(name="attachment_tbl")
@Where(clause = "deleted = false")
public class Attachment extends Base{

    @Id
    @SequenceGenerator(name = "attachmentSeq", sequenceName = "attachment_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attachmentSeq")
    @Column(name = "id")
    private Long id;

    @Column(name = "file_name", length = 50)
    private String fileName;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "file_type", columnDefinition = "smallint")
    private FileType fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "attach_time")
    private LocalDateTime attachTime;

    @Column(name = "description", length = 50)
    private String description;


    @ManyToOne
    @JoinColumn(name="username",nullable = true,foreignKey = @ForeignKey(name = "fk_attachment_user"))
    private User user;

    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = true,foreignKey = @ForeignKey(name = "fk_attachment_ticket"))
    private Ticket ticket;

}
