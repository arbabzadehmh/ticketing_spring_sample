package ir.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@ToString

public class ProfileUserDto {
    private Long id;

    @NotBlank(message = "{validation.firstName}")
    @Pattern(regexp = "^[A-Za-z\\s'-]*$", message = "{validation.namePattern}")
    private String firstName;

    @NotBlank(message = "{validation.lastName}")
    @Pattern(regexp = "^[A-Za-z\\s'-]*$", message = "{validation.namePattern}")
    private String lastName;

    @Pattern(regexp = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "{validation.email}")
    private String email;

    @Pattern(regexp = "^[0-9]{11,}$", message = "{validation.phone}")
    private String phone;


    // User fields
    private String username;
    private String password;
    private Set<String> roles;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;
    private boolean deleted;
    private LocalDateTime credentialsExpiryDate;
}
