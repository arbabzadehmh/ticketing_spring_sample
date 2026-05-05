package ir.service;

import ir.dto.ProfileUserDto;
import ir.model.entity.Profile;
import ir.model.enums.FileType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.web.multipart.MultipartFile;

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
    String getEmailByUsername(String username);
    Page<Profile> findByLastNameLike(String lastName, Pageable pageable);
    Page<Profile> findByUserUsernameLike(String username, Pageable pageable);
    Profile uploadOrUpdateProfilePicture(Long profileId, MultipartFile file, String username);
    void deleteProfilePicture(Long profileId);
    String getProfilePictureBase64(Long profileId);
    Pair<byte[], FileType> getProfilePictureBytes(Long profileId);
}
