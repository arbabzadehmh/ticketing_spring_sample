package ir.dto.mapper;

import ir.dto.ProfileUserDto;
import ir.model.entity.Profile;
import ir.model.entity.Role;
import ir.model.entity.User;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class ProfileMapper {

    public ProfileUserDto toDto(Profile profile) {
        User user = profile.getUser();

        return ProfileUserDto.builder()
                .id(profile.getId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .email(profile.getEmail())
                .phone(profile.getPhone())
                .username(user.getUsername())
                .password(null) // پسورد را به دلایل امنیتی برنمی‌گردانیم
                .roles(user.getRoleSet()
                        .stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .accountNonExpired(user.isAccountNonExpired())
                .accountNonLocked(user.isAccountNonLocked())
                .credentialsNonExpired(user.isCredentialsNonExpired())
                .enabled(user.isEnabled())
                .deleted(user.isDeleted())
                .credentialsExpiryDate(user.getCredentialsExpiryDate())
                .build();
    }

    public Profile toEntity(ProfileUserDto dto) {

        return Profile.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .build();
    }

    public void updateEntity(ProfileUserDto dto, Profile existingProfile) {
        // به‌روزرسانی فیلدهای پروفایل
        existingProfile.setFirstName(dto.getFirstName());
        existingProfile.setLastName(dto.getLastName());
        existingProfile.setEmail(dto.getEmail());
        existingProfile.setPhone(dto.getPhone());

        // به‌روزرسانی فیلدهای یوزر
        User existingUser = existingProfile.getUser();
        existingUser.setAccountNonExpired(dto.isAccountNonExpired());
        existingUser.setAccountNonLocked(dto.isAccountNonLocked());
        existingUser.setCredentialsNonExpired(dto.isCredentialsNonExpired());
        existingUser.setEnabled(dto.isEnabled());
        existingUser.setDeleted(dto.isDeleted());
        existingUser.setCredentialsExpiryDate(dto.getCredentialsExpiryDate());

    }

}
