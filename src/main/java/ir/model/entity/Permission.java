package ir.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Where;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@ToString

@Entity(name="permissionEntity")
@Table(name="permission_tbl")
@Where(clause = "deleted = false")
public class Permission extends Base {

    @Id
    @SequenceGenerator(name = "permissionSeq", sequenceName = "permission_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "permissionSeq")
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "{validation.permission}")
    @Pattern(regexp = "^$|^[A-Za-z0-9_\\s-]{2,50}$", message = "{validation.permissionPattern}")
    @Column(name = "permission", nullable = false, unique = true, length = 100)
    private String permissionName;
}
