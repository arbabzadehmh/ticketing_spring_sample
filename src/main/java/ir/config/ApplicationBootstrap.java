package ir.config;


import ir.model.entity.Permission;
import ir.model.entity.Profile;
import ir.model.entity.Role;
import ir.model.entity.User;
import ir.repository.PermissionRepository;
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

import java.util.HashSet;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationBootstrap {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissionRepository permissionRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void init() {

        if (userRepository.existsById("admin")) {
            log.info(" ---------->  admin is already exist.");
            return;
        }



        createPermissions();

        Role adminRole = createRole("ROLE_ADMIN");
        createRole("ROLE_MANAGER");
        createRole("ROLE_CUSTOMER");

        assignAdminPermissions(adminRole);

        createAdminUser(adminRole);

        log.info(" ---------->  Bootstrap completed.");
    }

    private void createPermissions() {
        createPermission("TICKET_CREATE");
        createPermission("TICKET_EDIT");
        createPermission("TICKET_DELETE");

        createPermission("BUILDING_CREATE");
        createPermission("BUILDING_EDIT");
        createPermission("BUILDING_DELETE");

        createPermission("TICKET_CHECK_SCORE");
        createPermission("TICKET_CHECK_CLOSE");

        createPermission("REPORT_VIEW");
    }

    private void createPermission(String name) {
        if (!permissionRepository.existsByPermissionName(name)) {
            permissionRepository.save(
                    Permission.builder()
                            .permissionName(name)
                            .build()
            );
        }
    }

    private Role createRole(String name) {
        return roleRepository.findById(name)
                .orElseGet(() ->
                        roleRepository.save(
                                Role.builder()
                                        .name(name)
                                        .build()
                        ));
    }

    private void assignAdminPermissions(Role adminRole) {
        adminRole.setPermissionSet(
                new HashSet<>(permissionRepository.findAll())
        );

        roleRepository.save(adminRole);
    }


    private void createAdminUser(Role adminRole) {

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

        log.info(" ---------->  Admin profile created");
    }
}
