package ir.service;

import ir.dto.ProfileUserDto;
import ir.model.entity.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProfileService {
    Profile save(Profile profile);
    Profile createProfileByCustomer(ProfileUserDto dto);
    Profile createProfileByAdmin(ProfileUserDto dto);
    Profile updateProfile(ProfileUserDto dto, Long profileId, boolean isAdmin);
    void deleteById(Long id);
    Page<Profile> findAll(Pageable pageable);
    Profile findById(Long id);
    Page<Profile> findByOrderByFirstNameAsc(String firstName, Pageable pageable);
    Page<Profile> findByOrderByLastNameAsc(String lastName, Pageable pageable);
    Profile findByUsername(String username);
    Page<Profile> findByLastNameLike(String lastName, Pageable pageable);
    Page<Profile> findByUserUsernameLike(String username, Pageable pageable);
}
