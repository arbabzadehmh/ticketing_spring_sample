package ir.model.entity;



import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "file_type", columnDefinition = "smallint")
    private FileType fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "attach_time")
    private LocalDateTime attachTime;

    @Column(name = "description", length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="username", foreignKey = @ForeignKey(name = "fk_attachment_user"))
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", foreignKey = @ForeignKey(name = "fk_attachment_message"))
    @JsonIgnore
    private Message message;

}
