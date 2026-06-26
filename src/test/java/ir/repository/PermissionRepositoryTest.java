package ir.repository;

import ir.model.entity.Permission;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PermissionRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private PermissionRepository repository;

    @Test
    void shouldFindPermissionByName() {

        Permission permission =
                Permission
                        .builder()
                        .permissionName("USER_CREATE")
                        .build();

        repository.save(permission);

        var result =
                repository.findByPermissionName(
                        "USER_CREATE"
                );

        assertTrue(result.isPresent());
    }

    @Test
    void shouldReturnTrueWhenPermissionExists() {

        Permission permission =
                Permission
                        .builder()
                        .permissionName("USER_DELETE")
                        .build();

        repository.save(permission);

        assertTrue(
                repository.existsByPermissionName(
                        "USER_DELETE"
                )
        );
    }

    @Test
    void shouldFindPermissionIgnoringCase() {

        Permission permission =
                Permission.builder()
                        .permissionName("USER_CREATE")
                        .build();

        repository.save(permission);

        var page =
                repository.findByPermissionNameContainingIgnoreCase(
                        "create",
                        PageRequest.of(0, 10)
                );

        assertEquals(
                1,
                page.getTotalElements()
        );
    }
}
