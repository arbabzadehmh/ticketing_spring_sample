package ir.service.impl;

import ir.controller.exception.DuplicatePermissionException;
import ir.controller.exception.EntityLockedException;
import ir.model.entity.Permission;
import ir.repository.PermissionRepository;
import ir.service.PermissionService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final EntityLockService entityLockService;

    public PermissionServiceImpl(PermissionRepository permissionRepository, EntityLockService entityLockService) {
        this.permissionRepository = permissionRepository;
        this.entityLockService = entityLockService;
    }

    @Transactional
    @Override
    public Permission save(Permission permission) {
        if(permissionRepository.existsByPermissionName(permission.getPermissionName())){
            throw new DuplicatePermissionException();
        }

        permission.setPermissionName(permission.getPermissionName().toUpperCase());

        return permissionRepository.save(permission);
    }

    @Transactional
    @Override
    public Permission update(Long id, Permission permission) {
        Permission existing = permissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found"));

        if (!Objects.equals(existing.getVersion(), permission.getVersion())) {
            throw new OptimisticLockException();
        }

        if (permissionRepository.existsByPermissionName(permission.getPermissionName())) {
            throw new DuplicatePermissionException();
        }

        existing.setPermissionName(permission.getPermissionName().toUpperCase());
        return permissionRepository.save(existing);
    }

    @Override
    public Page<Permission> findAll(Pageable pageable) {
        return permissionRepository.findAll(pageable);
    }

    @Override
    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    @Override
    public Page<Permission> findByPermissionNameContaining(String permissionName, Pageable pageable) {
        return permissionRepository.findByPermissionNameContainingIgnoreCase(permissionName, pageable);
    }

    @Transactional
    @Override
    public void deleteById(Long id) {

        String lockOwner = entityLockService.getLockOwner("permission", id);

        if (lockOwner != null) {
            throw new EntityLockedException();
        }

        Permission permission = permissionRepository
                .findById(id).orElseThrow(() -> new EntityNotFoundException("Permission not found"));

        permission.setDeleted(true);
        permissionRepository.save(permission);
    }

    public List<String> findAllNames() {
        return permissionRepository.findAll()
                .stream()
                .map(Permission::getPermissionName)
                .toList();
    }
}
