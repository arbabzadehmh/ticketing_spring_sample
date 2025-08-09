package ir.repository;

import ir.model.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,String> {
    Page<Role> findAll(Pageable pageable);
    List<Role> findAll();
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
    Page<Role> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
