package ir.service;

import ir.model.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PermissionService {
    Permission save(Permission permission);
    Permission update(Long id, Permission permission);
    void deleteById(Long id);
    Page<Permission> findAll(Pageable pageable);
    List<Permission> findAll();
    Page<Permission> findByPermissionNameContaining(String permissionName, Pageable pageable);
}
