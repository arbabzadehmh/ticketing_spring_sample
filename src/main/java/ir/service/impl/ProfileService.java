package ir.service.impl;

import ir.controller.exception.FileReadException;
import ir.controller.exception.FileStorageException;
import ir.dto.ProfileUserDto;
import ir.dto.mapper.ProfileMapper;
import ir.model.entity.Attachment;
import ir.model.entity.Profile;
import ir.model.entity.Role;
import ir.model.entity.User;
import ir.model.enums.FileType;
import ir.repository.AttachmentRepository;
import ir.repository.ProfileRepository;
import ir.repository.UserRepository;
import ir.service.RoleService;
import ir.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.util.Pair;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class ProfileService implements ir.service.ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final RoleService roleService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;

    public ProfileService(ProfileRepository profileRepository, ProfileMapper profileMapper, RoleService roleService, UserRepository userRepository, UserService userService, PasswordEncoder passwordEncoder, AttachmentRepository attachmentRepository, FileStorageService fileStorageService) {
        this.profileRepository = profileRepository;
        this.profileMapper = profileMapper;
        this.roleService = roleService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.attachmentRepository = attachmentRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    @Override
    public Profile save(Profile profile) {
        return profileRepository.save(profile);
    }

    @Transactional
    @Override
    public Profile createProfileByCustomer(ProfileUserDto dto) {
        Role customerRole = roleService.findByName("ROLE_CUSTOMER");
        User user =
                User
                        .builder()
                        .username(dto.getUsername())
                        .password(dto.getPassword())
                        .roleSet(Set.of(customerRole))
                        .build();

        user = userService.save(user);

        Profile profile = profileMapper.toEntity(dto);
        profile.setUser(user);

        return profileRepository.save(profile);
    }

    @Transactional
    @Override
    public Profile createProfileByAdmin(ProfileUserDto dto) {

        Set<Role> roles = dto.getRoles().stream()
                .map(roleService::findByName)
                .collect(Collectors.toSet());

        User user =
                User
                        .builder()
                        .username(dto.getUsername())
                        .password(dto.getPassword())
                        .roleSet(roles)
                        .build();


        user = userService.save(user);

        Profile profile = profileMapper.toEntity(dto);
        profile.setUser(user);


        return profileRepository.save(profile);
    }

    @Transactional
    @Override
    public Profile updateProfile(ProfileUserDto dto, Long profileId, boolean isAdminOrManager) {

        Profile existingProfile = profileRepository.findById(profileId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found"));

        if (!Objects.equals(existingProfile.getVersion(), dto.getVersion())) {
            throw new OptimisticLockException();
        }

        if (isAdminOrManager) {
            // اگر ادمین بود، همه‌چیز به‌جز username قابل آپدیت
            profileMapper.updateEntity(dto, existingProfile);

            // نقش‌ها
            if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
                Set<Role> roles = dto.getRoles().stream()
                        .map(roleService::findByName)
                        .collect(Collectors.toSet());
                existingProfile.getUser().setRoleSet(roles);
            }

            // اگر پسورد وارد شده بود، رمزگذاری و آپدیت
            if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                existingProfile.getUser().setPassword(passwordEncoder.encode(dto.getPassword()));
            }
        } else {
            // اگر مشتری بود، فقط اطلاعات پروفایل خودش را تغییر دهد
            existingProfile.setFirstName(dto.getFirstName());
            existingProfile.setLastName(dto.getLastName());
            existingProfile.setEmail(dto.getEmail());
            existingProfile.setPhone(dto.getPhone());

            // اگر پسورد وارد شده بود، رمزگذاری و آپدیت
            if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                existingProfile.getUser().setPassword(passwordEncoder.encode(dto.getPassword()));
            }
        }

        userService.edit(existingProfile.getUser());
        return profileRepository.save(existingProfile);
    }


    @Transactional
    @Override
    public void deleteById(Long id) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found"));

        // اگر عکس دارد، حذفش کن
        if (profile.getProfilePicture() != null) {
            fileStorageService.deleteById(profile.getProfilePicture().getMongoFileId());
            Attachment attachment = profile.getProfilePicture();
            attachment.setDeleted(true);
            attachmentRepository.save(attachment);
        }

        profile.setDeleted(true);
        profileRepository.save(profile);
    }

    @Override
    public Page<Profile> findAll(Pageable pageable) {
        return profileRepository.findAll(pageable);
    }

    @Override
    public Profile findById(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found"));  //in ExceptionWrapper error's message will change
    }

    @Override
    public Page<Profile> findByOrderByFirstNameAsc(String firstName, Pageable pageable) {
        return profileRepository.findByOrderByFirstNameAsc(firstName, pageable);
    }

    @Override
    public Page<Profile> findByOrderByLastNameAsc(String lastName, Pageable pageable) {
        return profileRepository.findByOrderByLastNameAsc(lastName, pageable);
    }

    @Override
    public Profile findByUsername(String username) {
        return profileRepository.findByUserUsername(username);
    }

    @Override
    public String getEmailByUsername(String username) {
        return profileRepository.findEmailByUserUsername(username);
    }


    @Override
    public Page<Profile> findByLastNameLike(String lastName, Pageable pageable) {
        return profileRepository.findByLastNameLike(lastName + "%", pageable);
    }

    @Override
    public Page<Profile> findByUserUsernameLike(String username, Pageable pageable) {
        return profileRepository.findByUserUsernameLike(username + "%", pageable);
    }

    @Transactional
    @Override
    public Profile uploadOrUpdateProfilePicture(Long profileId, MultipartFile file, String username) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found"));

        try {
            // حذف عکس قبلی در GridFS و دیتابیس
            if (profile.getProfilePicture() != null) {
                fileStorageService.deleteById(profile.getProfilePicture().getMongoFileId());
                attachmentRepository.delete(profile.getProfilePicture());
            }

            // ذخیره فایل در GridFS
            String mongoId = fileStorageService.store(file, username);

            Attachment attachment = Attachment.builder()
                    .fileName(file.getOriginalFilename())
                    .fileSize(file.getSize())
                    .fileType(mapContentTypeToFileType(file.getContentType()))
                    .attachTime(LocalDateTime.now())
                    .mongoFileId(mongoId)
                    .build();

            attachmentRepository.save(attachment);

            profile.setProfilePicture(attachment);
            return profileRepository.save(profile);
        } catch (IOException e) {
            throw new FileStorageException();
        }
    }

    @Transactional
    @Override
    public Profile deleteProfilePicture(Long profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found"));

        if (profile.getProfilePicture() != null) {
            fileStorageService.deleteById(profile.getProfilePicture().getMongoFileId());
            attachmentRepository.delete(profile.getProfilePicture());
            profile.setProfilePicture(null);
            return profileRepository.save(profile);
        }
        return profile;
    }

    // دریافت Base64 برای نمایش در مرورگر
    public String getProfilePictureBase64(Long profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found"));

        Attachment att = profile.getProfilePicture();
        if (att == null) return null;

        try {

            GridFsResource res = fileStorageService.getResource(att.getMongoFileId());
            if (res == null) return null;

            byte[] bytes = res.getInputStream().readAllBytes();
            return Base64.getEncoder().encodeToString(bytes);

        } catch (IOException e) {
            throw new FileReadException();
        }

    }

    public Pair<byte[], FileType> getProfilePictureBytes(Long profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found"));

        Attachment att = profile.getProfilePicture();
        if (att == null)
            throw new EntityNotFoundException("Profile picture not found");

        GridFsResource res = fileStorageService.getResource(att.getMongoFileId());
        if (res == null)
            throw new EntityNotFoundException("File not found in storage");

        try {
            byte[] bytes = res.getInputStream().readAllBytes();
            return Pair.of(bytes, att.getFileType());
        } catch (IOException e) {
            throw new FileReadException();
        }
    }


    private FileType mapContentTypeToFileType(String contentType) {
        if (contentType == null) return null;
        switch (contentType.toLowerCase()) {
            case "image/jpeg":
                return FileType.JPG;
            case "image/png":
                return FileType.PNG;
            case "image/bmp":
                return FileType.BMP;
            case "application/pdf":
                return FileType.PDF;
            case "text/plain":
                return FileType.TXT;
            default:
                return null;
        }
    }
}
