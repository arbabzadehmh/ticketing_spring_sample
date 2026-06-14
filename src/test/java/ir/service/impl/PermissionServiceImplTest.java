package ir.service.impl;

import ir.controller.exception.DuplicatePermissionException;
import ir.controller.exception.EntityLockedException;
import ir.model.entity.Permission;
import ir.repository.PermissionRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PermissionServiceImplTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private EntityLockService entityLockService;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    @Test
    void save_shouldThrowDuplicatePermissionException() {

        Permission permission = new Permission();
        permission.setPermissionName("USER_READ");

        when(permissionRepository
                .existsByPermissionName("USER_READ"))
                .thenReturn(true);

        assertThrows(
                DuplicatePermissionException.class,
                () -> permissionService.save(permission)
        );
    }

    @Test
    void save_shouldConvertNameToUpperCase() {

        Permission permission = new Permission();
        permission.setPermissionName("user_read");

        when(permissionRepository
                .existsByPermissionName("user_read"))
                .thenReturn(false);

        when(permissionRepository.save(any(Permission.class)))
                .thenAnswer(i -> i.getArgument(0));

        Permission saved =
                permissionService.save(permission);

        assertEquals(
                "USER_READ",
                saved.getPermissionName()
        );

        verify(permissionRepository)
                .save(permission);
    }

    @Test
    void update_shouldThrowWhenPermissionNotFound() {

        Permission permission = new Permission();

        when(permissionRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> permissionService.update(
                        1L,
                        permission
                )
        );
    }

    @Test
    void update_shouldThrowOptimisticLockException() {

        Permission existing = new Permission();
        existing.setVersion(1L);

        Permission updated = new Permission();
        updated.setVersion(2L);

        when(permissionRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        assertThrows(
                OptimisticLockException.class,
                () -> permissionService.update(
                        1L,
                        updated
                )
        );
    }

    @Test
    void update_shouldThrowDuplicatePermissionException() {

        Permission existing = new Permission();
        existing.setVersion(1L);

        Permission updated = new Permission();
        updated.setVersion(1L);
        updated.setPermissionName("USER_READ");

        when(permissionRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(permissionRepository
                .existsByPermissionName("USER_READ"))
                .thenReturn(true);

        assertThrows(
                DuplicatePermissionException.class,
                () -> permissionService.update(
                        1L,
                        updated
                )
        );
    }

    @Test
    void update_shouldUpdatePermissionName() {

        Permission existing = new Permission();
        existing.setVersion(1L);

        Permission updated = new Permission();
        updated.setVersion(1L);
        updated.setPermissionName("user_write");

        when(permissionRepository.findById(1L))
                .thenReturn(Optional.of(existing));

        when(permissionRepository
                .existsByPermissionName("user_write"))
                .thenReturn(false);

        when(permissionRepository.save(any(Permission.class)))
                .thenAnswer(i -> i.getArgument(0));

        Permission result =
                permissionService.update(
                        1L,
                        updated
                );

        assertEquals(
                "USER_WRITE",
                result.getPermissionName()
        );

        verify(permissionRepository)
                .save(existing);
    }

    @Test
    void findByName_shouldThrowWhenNotFound() {

        when(permissionRepository
                .findByPermissionName("USER_READ"))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> permissionService.findByName("USER_READ")
        );
    }

    @Test
    void findByName_shouldReturnPermission() {

        Permission permission = new Permission();

        when(permissionRepository
                .findByPermissionName("USER_READ"))
                .thenReturn(Optional.of(permission));

        Permission result =
                permissionService.findByName("USER_READ");

        assertNotNull(result);
    }

    @Test
    void deleteById_shouldThrowWhenLocked() {

        when(entityLockService
                .getLockOwner("permission", 1L))
                .thenReturn("admin");

        assertThrows(
                EntityLockedException.class,
                () -> permissionService.deleteById(1L)
        );

        verify(permissionRepository, never())
                .findById(anyLong());
    }

    @Test
    void deleteById_shouldThrowWhenPermissionNotFound() {

        when(entityLockService
                .getLockOwner("permission", 1L))
                .thenReturn(null);

        when(permissionRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> permissionService.deleteById(1L)
        );
    }

    @Test
    void deleteById_shouldMarkPermissionDeleted() {

        Permission permission = new Permission();

        when(entityLockService
                .getLockOwner("permission", 1L))
                .thenReturn(null);

        when(permissionRepository.findById(1L))
                .thenReturn(Optional.of(permission));

        permissionService.deleteById(1L);

        assertTrue(permission.isDeleted());

        verify(permissionRepository)
                .save(permission);
    }

    @Test
    void findAllNames_shouldReturnNames() {

        Permission p1 = new Permission();
        p1.setPermissionName("USER_READ");

        Permission p2 = new Permission();
        p2.setPermissionName("USER_WRITE");

        when(permissionRepository.findAll())
                .thenReturn(List.of(p1, p2));

        List<String> names =
                permissionService.findAllNames();

        assertEquals(2, names.size());

        assertTrue(names.contains("USER_READ"));

        assertTrue(names.contains("USER_WRITE"));
    }

}
