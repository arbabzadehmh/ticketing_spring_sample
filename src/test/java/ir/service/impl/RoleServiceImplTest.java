package ir.service.impl;

import ir.controller.exception.DuplicateRoleException;
import ir.controller.exception.EntityLockedException;
import ir.model.entity.Permission;
import ir.model.entity.Role;
import ir.repository.PermissionRepository;
import ir.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private EntityLockService entityLockService;

    @Mock
    private PermissionRepository permissionRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    void save_shouldThrowDuplicateRoleException() {

        Role role = new Role();
        role.setName("admin");

        when(roleRepository.existsByName("ROLE_ADMIN"))
                .thenReturn(true);

        assertThrows(DuplicateRoleException.class,
                () -> roleService.save(role));

    }

    @Test
    void save_shouldThrowWhenPermissionNotFound() {

        Role role = new Role();
        role.setName("admin");

        Permission permission = new Permission();
        permission.setPermissionName("USER_READ");

        role.setPermissionSet(Set.of(permission));

        when(roleRepository.existsByName("ROLE_ADMIN"))
                .thenReturn(false);

        when(permissionRepository.findByPermissionName("USER_READ"))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> roleService.save(role)
        );
    }

    @Test
    void save_shouldAddRolePrefix() {

        Role role = new Role();
        role.setName("admin");

        Permission permission = new Permission();
        permission.setPermissionName("USER_READ");

        role.setPermissionSet(Set.of(permission));

        when(roleRepository.existsByName("ROLE_ADMIN"))
                .thenReturn(false);

        when(permissionRepository.findByPermissionName("USER_READ"))
                .thenReturn(Optional.of(permission));

        when(roleRepository.save(any(Role.class)))
                .thenAnswer(i -> i.getArgument(0));

        Role saved = roleService.save(role);

        assertEquals(
                "ROLE_ADMIN",
                saved.getName()
        );
    }

    @Test
    void update_shouldThrowOptimisticLockException() {

        Role existingRole = new Role();
        existingRole.setVersion(1L);

        Role updatedRole = new Role();
        updatedRole.setVersion(2L);

        when(roleRepository.findByName("ROLE_ADMIN"))
                .thenReturn(Optional.of(existingRole));

        assertThrows(
                OptimisticLockException.class,
                () -> roleService.update(
                        "ROLE_ADMIN",
                        updatedRole
                )
        );
    }

    @Test
    void update_shouldUpdatePermissions() {

        Role existingRole = new Role();
        existingRole.setVersion(1L);

        Permission permission = new Permission();
        permission.setPermissionName("USER_READ");

        Role updatedRole = new Role();
        updatedRole.setVersion(1L);
        updatedRole.setPermissionSet(Set.of(permission));

        when(roleRepository.findByName("ROLE_ADMIN"))
                .thenReturn(Optional.of(existingRole));

        when(permissionRepository.findByPermissionName("USER_READ"))
                .thenReturn(Optional.of(permission));

        when(roleRepository.save(any(Role.class)))
                .thenAnswer(i -> i.getArgument(0));

        Role result = roleService.update("ROLE_ADMIN", updatedRole);

        assertEquals(1, result.getPermissionSet().size());

        verify(roleRepository)
                .save(existingRole);
    }

    @Test
    void deleteByName_shouldMarkRoleDeleted() {

        Role role = new Role();

        when(entityLockService.getLockOwner(
                "role",
                "ROLE_ADMIN"))
                .thenReturn(null);

        when(roleRepository.findByName("ROLE_ADMIN"))
                .thenReturn(Optional.of(role));

        roleService.deleteByName("ROLE_ADMIN");

        assertTrue(role.isDeleted());

        verify(roleRepository)
                .save(role);
    }

    @Test
    void deleteByName_shouldThrowWhenRoleNotFound() {

        when(entityLockService.getLockOwner("role", "ROLE_ADMIN"))
                .thenReturn(null);

        when(roleRepository.findByName("ROLE_ADMIN"))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> roleService.deleteByName("ROLE_ADMIN")
        );
    }

    @Test
    void deleteByName_shouldThrowWhenRoleLocked() {

        when(entityLockService.getLockOwner(
                "role",
                "ROLE_ADMIN"))
                .thenReturn("user1");

        assertThrows(
                EntityLockedException.class,
                () -> roleService.deleteByName("ROLE_ADMIN")
        );

        verify(roleRepository, never())
                .findByName(anyString());

        verify(roleRepository, never())
                .save(any(Role.class));
    }
}
