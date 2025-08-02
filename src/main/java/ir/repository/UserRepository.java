package ir.repository;

import ir.model.entity.Role;
import ir.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsUserByUsername(String username);
    //    List<User> findByFirstNameIsLikeAndLastNameIsLike(String firstName, String lastName);
    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameAndPassword(String username, String password);
    List<User> findByRoleSetContaining(Role role);
    List<User> findByRoleSetName(String roleName);

}
