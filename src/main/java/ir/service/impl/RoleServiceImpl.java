package ir.service.impl;

import ir.controller.exception.DuplicateRoleException;
import ir.model.entity.Permission;
import ir.model.entity.Role;
import ir.repository.PermissionRepository;
import ir.repository.RoleRepository;
import ir.service.RoleService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleServiceImpl(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Transactional
    @Override
    public Role save(Role role) {

        if (roleRepository.existsByName("ROLE_" + role.getName().toUpperCase())) {
            throw new DuplicateRoleException();
        }

        Set<Permission> permissions = role.getPermissionSet().stream()
                .map(p -> permissionRepository.findByPermissionName(p.getPermissionName())
                        .orElseThrow(() -> new EntityNotFoundException("Permission not found: " + p.getPermissionName())))
                .collect(Collectors.toSet());

        role.setPermissionSet(permissions);

        role.setName("ROLE_" + role.getName().toUpperCase());

        return roleRepository.save(role);
    }

    @Transactional
    @Override
    public Role update(String name, Role updatedRole) {
        Role existingRole = roleRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));

        Set<Permission> permissions = updatedRole.getPermissionSet().stream()
                .map(p -> permissionRepository.findByPermissionName(p.getPermissionName())
                        .orElseThrow(() -> new EntityNotFoundException("Permission not found: " + p.getPermissionName())))
                .collect(Collectors.toSet());

        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>" + permissions);

        existingRole.setPermissionSet(permissions);

        System.out.println(existingRole);
        return roleRepository.save(existingRole);
    }

    @Transactional
    @Override
    public void deleteByName(String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));

        role.setDeleted(true);
        roleRepository.save(role);
    }

    @Override
    public Role findByName(String roleName) {
        return roleRepository.findByName(roleName).orElse(null);
    }

    @Override
    public Page<Role> findAll(Pageable pageable) {
        return roleRepository.findAll(pageable);
    }

    @Override
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Override
    public Page<Role> findByNameContaining(String name, Pageable pageable) {
        return roleRepository.findByNameContainingIgnoreCase(name, pageable);
    }
}
