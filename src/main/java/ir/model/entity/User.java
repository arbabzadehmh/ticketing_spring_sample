package ir.model.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
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
    @Column(name = "username", unique = true, nullable = false)
    private String username;

//    @JsonIgnore
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
    private Set<Role> roleSet;

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
        if(roleSet == null){
            roleSet = new HashSet<>();
        }
        roleSet.add(role);
    }

}
