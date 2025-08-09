package ir.repository;

import ir.model.entity.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Page<Permission> findAll(Pageable pageable);
    List<Permission> findAll();
    Optional<Permission> findByPermissionName(String name);
    boolean existsByPermissionName(String name);
    Page<Permission> findByPermissionNameContainingIgnoreCase(String permissionName, Pageable pageable);
}
