package ir.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@ToString

@Entity(name="roleEntity")
@Table(name="role_tbl")
@Where(clause = "deleted = false")
public class Role extends Base {
    @Id
    @NotBlank(message = "{validation.role}")
    @Pattern(regexp = "^$|^[A-Za-z0-9_\\s-]{2,50}$", message = "{validation.rolePattern}")
    @Column(name = "name", length = 30)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permission_tbl",
            joinColumns = @JoinColumn(name = "role_name"),
            inverseJoinColumns = @JoinColumn(name = "permission"),
            foreignKey = @ForeignKey(name = "fk_role_permission"),
            inverseForeignKey = @ForeignKey(name = "fk_inverse_role_permission")
    )
    private Set<Permission> permissionSet = new HashSet<>();

    public void addPermission(Permission permission) {
        permissionSet.add(permission);
    }

    // Constructor to handle deserialization from string
    public Role(String name) {
        this.name = name;
    }
}
