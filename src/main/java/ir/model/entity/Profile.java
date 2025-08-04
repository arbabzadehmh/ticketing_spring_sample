package ir.model.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Where;


@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@ToString

@Entity(name="profileEntity")
@Table(name="profile_tbl")
@Where(clause = "deleted = false")
public class Profile extends Base {

    @Id
    @SequenceGenerator(name = "profileSeq", sequenceName = "profile_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "profileSeq")
    @Column(name = "id")
    private Long id;

//    @NotBlank(message = "{validation.firstName}")
//    @Pattern(regexp = "^$|^[A-Za-z\\s'-]{2,50}$", message = "{validation.namePattern}")
    @Column(name = "first_name", nullable = false, length = 30)
    private String firstName;

//    @NotBlank(message = "{validation.lastName}")
//    @Pattern(regexp = "^$|^[A-Za-z\\s'-]{2,50}$", message = "{validation.namePattern}")
    @Column(name = "last_name", nullable = false, length = 30)
    private String lastName;

//    @Pattern(regexp = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "{validation.email}")
    @Column(name = "email", length = 50)
    private String email;

//    @Pattern(regexp = "^[0-9]{11,}$", message = "{validation.phone}")
    @Column(name = "phone", length = 15)
    private String phone;

    @OneToOne
    @JoinColumn(
            name = "username",
            foreignKey = @ForeignKey(name = "fk_profile_user")
    )
    private User user;

}
