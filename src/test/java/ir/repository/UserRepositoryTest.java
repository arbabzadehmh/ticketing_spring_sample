package ir.repository;

import ir.model.entity.Permission;
import ir.model.entity.Role;
import ir.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Test
    void shouldReturnTrueWhenUsernameExists() {

        User user =
                User
                        .builder()
                        .username("ali")
                        .password("123")
                        .build();

        repository.save(user);

        assertTrue(
                repository.existsUserByUsername("ali")
        );
    }

    @Test
    void shouldFindByUsername() {

        User user = User.builder()
                .username("ali")
                .password("123")
                .build();

        repository.save(user);

        Optional<User> result =
                repository.findByUsername("ali");

        assertTrue(result.isPresent());
        assertEquals("ali", result.get().getUsername());
    }

    @Test
    void shouldFindByUsernameAndPassword() {

        User user =
                User
                        .builder()
                        .username("ali")
                        .password("123")
                        .build();

        repository.save(user);

        Optional<User> result =
                repository.findByUsernameAndPassword(
                        "ali",
                        "123"
                );

        assertTrue(result.isPresent());
    }

    @Test
    void shouldFindUsersByRoleContaining() {

        Role role =
                Role
                        .builder()
                        .name("ROLE_ADMIN")
                        .build();

        roleRepository.saveAndFlush(role);

        User user =
                User
                        .builder()
                        .username("ali")
                        .password("123")
                        .build();

        user.addRole(role);

        repository.save(user);

        List<User> result =
                repository.findByRoleSetContaining(role);

        assertEquals(1, result.size());
        assertEquals("ali", result.get(0).getUsername());
    }

    @Test
    void shouldFindUsersByRoleName() {

        Role role =
                Role
                        .builder()
                        .name("ROLE_ADMIN")
                        .build();

        roleRepository.saveAndFlush(role);

        User user = User.builder()
                .username("ali")
                .password("123")
                .build();

        user.addRole(role);

        repository.save(user);

        List<User> result =
                repository.findByRoleSetName(
                        "ROLE_ADMIN"
                );

        assertEquals(1, result.size());
    }

    @Test
    void shouldFindAdminsWithPermission() {

        Permission permission =
                Permission
                        .builder()
                        .permissionName("BUILDING_READ")
                        .build();

        permissionRepository.saveAndFlush(permission);

        Role adminRole =
                Role
                        .builder()
                        .name("ROLE_ADMIN")
                        .build();

        adminRole.setPermissionSet(
                Set.of(permission)
        );

        roleRepository.saveAndFlush(adminRole);

        User admin =
                User.builder()
                        .username("admin")
                        .password("123")
                        .build();

        admin.addRole(adminRole);

        repository.save(admin);

        List<User> result =
                repository.findAdminsWithPermission(
                        "BUILDING_READ"
                );

        assertEquals(1, result.size());

        assertEquals(
                "admin",
                result.get(0).getUsername()
        );
    }

    @Test
    void shouldReturnEmptyWhenPermissionNotExists() {

        List<User> result =
                repository.findAdminsWithPermission(
                        "UNKNOWN_PERMISSION"
                );

        assertTrue(result.isEmpty());
    }
}
