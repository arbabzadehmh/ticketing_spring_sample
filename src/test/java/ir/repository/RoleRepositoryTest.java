package ir.repository;

import ir.model.entity.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RoleRepositoryTest extends BaseRepositoryTest{

    @Autowired
    private RoleRepository repository;

    @Test
    void shouldFindRoleByName() {

        Role role =
                Role.builder()
                        .name("ROLE_ADMIN")
                        .build();

        repository.save(role);

        var result =
                repository.findByName("ROLE_ADMIN");

        assertTrue(result.isPresent());
    }

    @Test
    void shouldReturnTrueWhenRoleExists() {

        Role role =
                Role.builder()
                        .name("ROLE_MANAGER")
                        .build();

        repository.save(role);

        assertTrue(
                repository.existsByName(
                        "ROLE_MANAGER"
                )
        );
    }

    @Test
    void shouldFindRoleIgnoringCase() {

        Role role =
                Role.builder()
                        .name("ROLE_ADMIN")
                        .build();

        repository.save(role);

        var page =
                repository.findByNameContainingIgnoreCase(
                        "admin",
                        PageRequest.of(0,10)
                );

        assertEquals(
                1,
                page.getTotalElements()
        );
    }
}
