package ir.service;

import ir.model.entity.Role;

import java.util.List;

public interface RoleService {
    void save(Role role);
    void update(Role role);
    void delete(String roleName);
    Role findByName(String roleName);
    List<Role> findAll();
}
