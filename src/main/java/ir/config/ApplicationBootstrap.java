package ir.config;


import ir.model.entity.Profile;
import ir.model.entity.Role;
import ir.model.entity.User;
import ir.repository.ProfileRepository;
import ir.repository.RoleRepository;
import ir.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationBootstrap {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {

        if (userRepository.existsById("admin")) {
            log.info("admin is already exist.");
            return;
        }

        Role adminRole = roleRepository.findById("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .name("ROLE_ADMIN")
                                .build()
                ));


        User adminUser = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin"))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        adminUser.addRole(adminRole);

        User adminSavedUser = userRepository.save(adminUser);


        Profile profile = Profile.builder()
                .firstName("System")
                .lastName("Administrator")
                .email(null)
                .phone(null)
                .user(adminSavedUser)
                .build();

        profileRepository.save(profile);

        log.info("Admin profile created");
    }
}
