package ir.service.impl;

import ir.dto.ProfileUserDto;
import ir.dto.mapper.ProfileMapper;
import ir.model.entity.Profile;
import ir.model.entity.Role;
import ir.model.entity.User;
import ir.repository.ProfileRepository;
import ir.repository.UserRepository;
import ir.service.RoleService;
import ir.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;


@Service
public class ProfileService implements ir.service.ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final RoleService roleService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(ProfileRepository profileRepository, ProfileMapper profileMapper, RoleService roleService, UserRepository userRepository, UserService userService, PasswordEncoder passwordEncoder) {
        this.profileRepository = profileRepository;
        this.profileMapper = profileMapper;
        this.roleService = roleService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @Override
    public Profile save(Profile profile) {
        return profileRepository.save(profile);
    }

    @Transactional
    @Override
    public Profile createProfileByCustomer(ProfileUserDto dto) {
        Role customerRole = roleService.findByName("ROLE_CUSTOMER");
        User user =
                User
                        .builder()
                        .username(dto.getUsername())
                        .password(dto.getPassword())
                        .roleSet(Set.of(customerRole))
                        .build();

        user = userService.save(user);

        Profile profile = profileMapper.toEntity(dto);
        profile.setUser(user);

        return profileRepository.save(profile);
    }

    @Transactional
    @Override
    public Profile createProfileByAdmin(ProfileUserDto dto) {

        Set<Role> roles = dto.getRoles().stream()
                .map(roleService::findByName)
                .collect(Collectors.toSet());

        User user =
                User
                        .builder()
                        .username(dto.getUsername())
                        .password(dto.getPassword())
                        .roleSet(roles)
                        .build();


        user = userService.save(user);

        Profile profile = profileMapper.toEntity(dto);
        profile.setUser(user);


        return profileRepository.save(profile);
    }

    @Transactional
    @Override
    public Profile updateProfile(ProfileUserDto dto, Long profileId, boolean isAdminOrManager) {

        Profile existingProfile = profileRepository.findById(profileId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found"));

        if (isAdminOrManager) {
            // اگر ادمین بود، همه‌چیز به‌جز username قابل آپدیت
            profileMapper.updateEntity(dto, existingProfile);

            // نقش‌ها
            if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
                Set<Role> roles = dto.getRoles().stream()
                        .map(roleService::findByName)
                        .collect(Collectors.toSet());
                existingProfile.getUser().setRoleSet(roles);
            }

            // اگر پسورد وارد شده بود، رمزگذاری و آپدیت
            if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                existingProfile.getUser().setPassword(passwordEncoder.encode(dto.getPassword()));
            }
        } else {
            // اگر مشتری بود، فقط اطلاعات پروفایل خودش را تغییر دهد
            existingProfile.setFirstName(dto.getFirstName());
            existingProfile.setLastName(dto.getLastName());
            existingProfile.setEmail(dto.getEmail());
            existingProfile.setPhone(dto.getPhone());

            // اگر پسورد وارد شده بود، رمزگذاری و آپدیت
            if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                existingProfile.getUser().setPassword(passwordEncoder.encode(dto.getPassword()));
            }
        }

        userService.edit(existingProfile.getUser());
        return profileRepository.save(existingProfile);
    }


    @Transactional
    @Override
    public void deleteById(Long id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found"));

        profile.setDeleted(true);
        profileRepository.save(profile);
    }

    @Override
    public Page<Profile> findAll(Pageable pageable) {
        return profileRepository.findAll(pageable);
    }

    @Override
    public Profile findById(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found"));  //in ExceptionWrapper error's message will change
    }

    @Override
    public Page<Profile> findByOrderByFirstNameAsc(String firstName, Pageable pageable) {
        return profileRepository.findByOrderByFirstNameAsc(firstName, pageable);
    }

    @Override
    public Page<Profile> findByOrderByLastNameAsc(String lastName, Pageable pageable) {
        return profileRepository.findByOrderByLastNameAsc(lastName, pageable);
    }

    @Override
    public Profile findByUsername(String username) {
        return profileRepository.findByUserUsername(username);
    }

    @Override
    public Page<Profile> findByLastNameLike(String lastName, Pageable pageable) {
        return profileRepository.findByLastNameLike( lastName + "%" , pageable);
    }

    @Override
    public Page<Profile> findByUserUsernameLike(String username, Pageable pageable) {
        return profileRepository.findByUserUsernameLike(  username + "%", pageable);
    }
}
