package ir.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@ToString

@Entity(name="userEntity")
@Table(name="user_tbl")
@Where(clause = "deleted = false")
public class User extends Base {
    @Id
//    @NotBlank(message = "{validation.username}")
//    @Pattern(regexp = "^$|^[a-zA-Z\\d._-]{3,30}$", message = "{validation.usernamePattern}")
    @Column(name = "username", unique = true, nullable = false)
    private String username;

//    @NotBlank(message = "{validation.password}")
//    @Pattern(regexp = "^$|^[a-zA-Z\\d@_]{3,15}$", message = "{validation.passwordPattern}")
    @Column(name = "password", nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role_tbl",
            joinColumns = @JoinColumn(name = "username"),
            inverseJoinColumns = @JoinColumn(name = "role_name"),
            foreignKey = @ForeignKey(name = "fk_user_role"),
            inverseForeignKey = @ForeignKey(name = "fk_inverse_user_role")
    )
    private Set<Role> roleSet = new HashSet<>();

    @Column(name = "account_non_expired")
    private boolean accountNonExpired = true;

    @Column(name = "account_non_locked")
    private boolean accountNonLocked = true;

    @Column(name = "credentials_non_expired")
    private boolean credentialsNonExpired = true;

    @Column(name = "enabled")
    private boolean enabled = true;

    @Column(name = "credentials_expiry_date")
    private LocalDateTime credentialsExpiryDate;

    public void addRole(Role role){
        roleSet.add(role);
    }

}
