package ir.service;

import ir.model.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoleService {
    Role save(Role role);
    Role update(String name, Role role);
    void deleteByName(String roleName);
    Role findByName(String roleName);
    Page<Role> findAll(Pageable pageable);
    List<Role> findAll();
}
