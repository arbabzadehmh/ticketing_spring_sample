package ir.service.impl;

import ir.controller.exception.DuplicatePermissionException;
import ir.model.entity.Permission;
import ir.repository.PermissionRepository;
import ir.service.PermissionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionServiceImpl(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
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

    @Transactional
    @Override
    public void deleteById(Long id) {
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
