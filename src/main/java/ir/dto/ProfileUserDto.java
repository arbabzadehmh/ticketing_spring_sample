package ir.dto;

import ir.validation.OnCreate;
import ir.validation.OnUpdate;
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

    @NotBlank(message = "{validation.firstName}", groups = {OnCreate.class, OnUpdate.class})
    @Pattern(regexp = "^$|^[A-Za-z\\s'-]{2,50}", message = "{validation.namePattern}", groups = {OnCreate.class, OnUpdate.class})
    private String firstName;

    @NotBlank(message = "{validation.lastName}", groups = {OnCreate.class, OnUpdate.class})
    @Pattern(regexp = "^$|^[A-Za-z\\s'-]{2,50}", message = "{validation.namePattern}", groups = {OnCreate.class, OnUpdate.class})
    private String lastName;

    @Pattern(regexp = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$", message = "{validation.email}", groups = {OnCreate.class, OnUpdate.class})
    private String email;

    @Pattern(regexp = "^[0-9]{11,}$", message = "{validation.phone}", groups = {OnCreate.class, OnUpdate.class})
    private String phone;


    // User fields
    @NotBlank(message = "{validation.username}", groups = OnCreate.class)
    @Pattern(regexp = "^$|^[a-zA-Z\\\\d._-]{3,30}$", message = "{validation.usernamePattern}", groups = OnCreate.class)
    private String username;

    @NotBlank(message = "{validation.password}", groups = OnCreate.class)
    @Pattern(regexp = "^$|^[a-zA-Z\\d@_]{3,15}$", message = "{validation.passwordPattern}", groups = {OnCreate.class, OnUpdate.class})
    private String password;

    private Set<String> roles;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;
    private boolean deleted;
    private LocalDateTime credentialsExpiryDate;
}
