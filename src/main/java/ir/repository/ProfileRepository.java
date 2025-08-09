package ir.repository;

import ir.model.entity.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Page<Profile> findAll(Pageable pageable);
    Page<Profile> findByOrderByFirstNameAsc(String firstName, Pageable pageable);
    Page<Profile> findByOrderByLastNameAsc(String firstName, Pageable pageable);
    Profile findByUserUsername(String username);
    Page<Profile> findByLastNameLike(String lastName, Pageable pageable);
    Page<Profile> findByUserUsernameLike(String username, Pageable pageable);
}
