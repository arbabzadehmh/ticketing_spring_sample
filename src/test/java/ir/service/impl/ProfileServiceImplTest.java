package ir.service.impl;

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
import ir.service.RoleService;
import ir.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceImplTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserService userService;

    @Mock
    private RoleService roleService;

    @Mock
    private ProfileMapper profileMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EntityLockService entityLockService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private AttachmentRepository attachmentRepository;

    @Spy
    @InjectMocks
    private ProfileService profileService;


    @Test
    void findAll_shouldReturnPage() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<Profile> page =
                new PageImpl<>(List.of(new Profile()));

        when(profileRepository.findAll(pageable))
                .thenReturn(page);

        Page<Profile> result = profileService.findAll(pageable);

        assertEquals(1, result.getTotalElements());

        verify(profileRepository)
                .findAll(pageable);
    }


    @Test
    void findById_shouldReturnProfile() {

        Profile profile = new Profile();

        when(profileRepository.findById(1L))
                .thenReturn(Optional.of(profile));

        Profile result = profileService.findById(1L);

        assertEquals(profile, result);
    }


    @Test
    void findById_shouldThrowException() {

        when(profileRepository.findById(1L))
                .thenReturn(Optional.empty());


        assertThrows(
                EntityNotFoundException.class,
                () -> profileService.findById(1L)
        );
    }


    @Test
    void findByUsername_shouldReturnProfile() {

        Profile profile = new Profile();

        when(profileRepository.findByUserUsername("john"))
                .thenReturn(profile);

        Profile result = profileService.findByUsername("john");

        assertEquals(profile, result);
    }


    @Test
    void getEmailByUsername_shouldReturnEmail() {

        when(profileRepository.findEmailByUserUsername("john"))
                .thenReturn("john@test.com");

        String result =
                profileService.getEmailByUsername("john");


        assertEquals("john@test.com", result);
    }


    @Test
    void findByLastNameLike_shouldCallRepository() {

        Pageable pageable =
                PageRequest.of(0,10);

        Page<Profile> page =
                new PageImpl<>(List.of());

        when(profileRepository.findByLastNameLike(
                "Ali%",
                pageable))
                .thenReturn(page);


        Page<Profile> result =
                profileService.findByLastNameLike(
                        "Ali",
                        pageable
                );


        assertEquals(page, result);
    }


    @Test
    void findByUserUsernameLike_shouldCallRepository() {

        Pageable pageable =
                PageRequest.of(0,10);

        Page<Profile> page =
                new PageImpl<>(List.of());

        when(profileRepository.findByUserUsernameLike(
                "admin%",
                pageable))
                .thenReturn(page);


        Page<Profile> result =
                profileService.findByUserUsernameLike(
                        "admin",
                        pageable
                );


        assertEquals(page, result);
    }

    @Test
    void createProfileByCustomer_shouldCreateProfileWithCustomerRole() {

        ProfileUserDto dto = new ProfileUserDto();

        dto.setUsername("john");
        dto.setPassword("123");


        Role role = new Role();
        role.setName("ROLE_CUSTOMER");


        User savedUser = new User();
        savedUser.setUsername("john");


        Profile profile = new Profile();


        when(roleService.findByName("ROLE_CUSTOMER"))
                .thenReturn(role);

        when(userService.save(any(User.class)))
                .thenReturn(savedUser);

        when(profileMapper.toEntity(dto))
                .thenReturn(profile);

        when(profileRepository.save(profile))
                .thenReturn(profile);


        Profile result =
                profileService.createProfileByCustomer(dto);


        assertEquals(savedUser, result.getUser());


        verify(roleService)
                .findByName("ROLE_CUSTOMER");

        verify(userService)
                .save(any(User.class));

        verify(profileMapper)
                .toEntity(dto);

        verify(profileRepository)
                .save(profile);
    }

    @Test
    void createProfileByAdmin_shouldCreateProfileWithRoles() {

        ProfileUserDto dto = new ProfileUserDto();

        dto.setUsername("admin");
        dto.setPassword("123");
        dto.setRoles(
                Set.of(
                        "ROLE_ADMIN",
                        "ROLE_MANAGER"
                )
        );


        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");


        Role managerRole = new Role();
        managerRole.setName("ROLE_MANAGER");


        User savedUser = new User();
        savedUser.setUsername("admin");


        Profile profile = new Profile();


        when(roleService.findByName("ROLE_ADMIN"))
                .thenReturn(adminRole);

        when(roleService.findByName("ROLE_MANAGER"))
                .thenReturn(managerRole);


        when(userService.save(any(User.class)))
                .thenReturn(savedUser);


        when(profileMapper.toEntity(dto))
                .thenReturn(profile);


        when(profileRepository.save(profile))
                .thenReturn(profile);


        Profile result =
                profileService.createProfileByAdmin(dto);


        assertEquals(savedUser, result.getUser());


        verify(roleService)
                .findByName("ROLE_ADMIN");

        verify(roleService)
                .findByName("ROLE_MANAGER");

        verify(userService)
                .save(any(User.class));

        verify(profileRepository)
                .save(profile);
    }

    @Test
    void updateProfile_shouldThrowWhenProfileNotFound() {

        ProfileUserDto dto = new ProfileUserDto();

        when(profileRepository.findById(1L))
                .thenReturn(Optional.empty());


        assertThrows(
                EntityNotFoundException.class,
                () -> profileService.updateProfile(
                        dto,
                        1L,
                        true
                )
        );
    }

    @Test
    void updateProfile_shouldThrowOptimisticLockException() {

        Profile profile = new Profile();
        profile.setVersion(1L);

        ProfileUserDto dto = new ProfileUserDto();
        dto.setVersion(2L);


        when(profileRepository.findById(1L))
                .thenReturn(Optional.of(profile));


        assertThrows(
                OptimisticLockException.class,
                () -> profileService.updateProfile(
                        dto,
                        1L,
                        true
                )
        );
    }

    @Test
    void updateProfile_admin_shouldUpdateProfile() {

        Profile profile = new Profile();

        User user = new User();
        profile.setUser(user);

        profile.setVersion(1L);


        ProfileUserDto dto = new ProfileUserDto();
        dto.setVersion(1L);


        when(profileRepository.findById(1L))
                .thenReturn(Optional.of(profile));

        when(profileRepository.save(profile))
                .thenReturn(profile);


        Profile result =
                profileService.updateProfile(
                        dto,
                        1L,
                        true
                );


        assertEquals(profile, result);


        verify(profileMapper)
                .updateEntity(dto, profile);

        verify(userService)
                .edit(user);

        verify(profileRepository)
                .save(profile);
    }

    @Test
    void updateProfile_admin_shouldUpdateRoles() {

        Profile profile = new Profile();

        User user = new User();
        profile.setUser(user);

        profile.setVersion(1L);


        ProfileUserDto dto = new ProfileUserDto();
        dto.setVersion(1L);
        dto.setRoles(Set.of("ROLE_ADMIN"));


        Role role = new Role();
        role.setName("ROLE_ADMIN");


        when(profileRepository.findById(1L))
                .thenReturn(Optional.of(profile));

        when(roleService.findByName("ROLE_ADMIN"))
                .thenReturn(role);


        profileService.updateProfile(
                dto,
                1L,
                true
        );


        assertTrue(
                user.getRoleSet().contains(role)
        );

        verify(roleService)
                .findByName("ROLE_ADMIN");
    }

    @Test
    void updateProfile_admin_shouldEncodePassword() {

        Profile profile = new Profile();

        User user = new User();
        profile.setUser(user);

        profile.setVersion(1L);


        ProfileUserDto dto = new ProfileUserDto();
        dto.setVersion(1L);
        dto.setPassword("1234");


        when(profileRepository.findById(1L))
                .thenReturn(Optional.of(profile));

        when(passwordEncoder.encode("1234"))
                .thenReturn("encoded-password");


        profileService.updateProfile(
                dto,
                1L,
                true
        );


        assertEquals(
                "encoded-password",
                user.getPassword()
        );

        verify(passwordEncoder)
                .encode("1234");
    }

    @Test
    void updateProfile_shouldUpdateOwnProfileByCustomer() {

        ProfileUserDto dto = new ProfileUserDto();
        dto.setFirstName("Ali");
        dto.setLastName("Ahmadi");
        dto.setEmail("ali@test.com");
        dto.setPhone("09120000000");
        dto.setVersion(1L);

        User user = new User();
        user.setUsername("customer");

        Profile profile = new Profile();
        profile.setUser(user);
        profile.setVersion(1L);


        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("customer");

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);


        when(profileRepository.findById(1L))
                .thenReturn(Optional.of(profile));

        when(profileRepository.save(profile))
                .thenReturn(profile);


        Profile result = profileService.updateProfile(
                dto,
                1L,
                false
        );


        assertEquals("Ali", result.getFirstName());
        assertEquals("Ahmadi", result.getLastName());
        assertEquals("ali@test.com", result.getEmail());
        assertEquals("09120000000", result.getPhone());

        verify(userService)
                .edit(user);

        verify(profileRepository)
                .save(profile);
    }

    @Test
    void updateProfile_shouldChangePasswordByCustomer() {

        ProfileUserDto dto = new ProfileUserDto();
        dto.setPassword("123456");
        dto.setVersion(1L);


        User user = new User();
        user.setUsername("customer");

        Profile profile = new Profile();
        profile.setUser(user);
        profile.setVersion(1L);


        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("customer");

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);


        when(profileRepository.findById(1L))
                .thenReturn(Optional.of(profile));

        when(passwordEncoder.encode("123456"))
                .thenReturn("encodedPassword");

        when(profileRepository.save(profile))
                .thenReturn(profile);


        Profile result = profileService.updateProfile(
                dto,
                1L,
                false
        );


        assertEquals(
                "encodedPassword",
                result.getUser().getPassword()
        );

        verify(passwordEncoder)
                .encode("123456");

        verify(userService)
                .edit(user);

        verify(profileRepository)
                .save(profile);
    }

    @Test
    void updateProfile_shouldThrowAccessDeniedWhenCustomerUpdatesOtherProfile() {

        ProfileUserDto dto = new ProfileUserDto();
        dto.setVersion(1L);


        User user = new User();
        user.setUsername("anotherUser");


        Profile profile = new Profile();
        profile.setUser(user);
        profile.setVersion(1L);


        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("customer");

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);


        when(profileRepository.findById(1L))
                .thenReturn(Optional.of(profile));


        assertThrows(
                AccessDeniedException.class,
                () -> profileService.updateProfile(
                        dto,
                        1L,
                        false
                )
        );


        verify(userService, never())
                .edit(any(User.class));

        verify(profileRepository, never())
                .save(any(Profile.class));
    }

    @Test
    void uploadOrUpdateProfilePicture_shouldSaveNewPicture() throws Exception {

        Profile profile = new Profile();

        User user = new User();
        user.setUsername("ali");
        profile.setUser(user);

        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename())
                .thenReturn("pic.jpg");

        when(file.getSize())
                .thenReturn(200L);

        when(file.getContentType())
                .thenReturn("image/jpeg");


        when(profileRepository.findById(1L))
                .thenReturn(Optional.of(profile));

        doNothing()
                .when(profileService)
                .validateProfileAccess(profile);

        when(fileStorageService.store(file, "ali"))
                .thenReturn("mongo123");

        when(profileRepository.save(any(Profile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Profile result =
                profileService.uploadOrUpdateProfilePicture(1L, file, "ali");

        assertNotNull(result.getProfilePicture());
        assertEquals("pic.jpg",
                result.getProfilePicture().getFileName());
        assertEquals(FileType.JPG,
                result.getProfilePicture().getFileType());

        verify(attachmentRepository)
                .save(any(Attachment.class));

        verify(profileRepository)
                .save(profile);
    }

    @Test
    void uploadOrUpdateProfilePicture_shouldThrowWhenProfileNotFound() {

        MultipartFile file = mock(MultipartFile.class);

        when(profileRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> profileService.uploadOrUpdateProfilePicture(
                        1L,
                        file,
                        "ali"
                )
        );
    }

    @Test
    void uploadOrUpdateProfilePicture_shouldDeleteOldPicture()
            throws Exception {

        Attachment oldAttachment = new Attachment();
        oldAttachment.setMongoFileId("old123");

        Profile profile = new Profile();
        profile.setProfilePicture(oldAttachment);

        User user = new User();
        user.setUsername("ali");
        profile.setUser(user);

        MultipartFile file = mock(MultipartFile.class);

        when(file.getOriginalFilename())
                .thenReturn("new.png");

        when(file.getSize())
                .thenReturn(200L);

        when(file.getContentType())
                .thenReturn("image/png");

        when(profileRepository.findById(1L))
                .thenReturn(Optional.of(profile));

        doNothing()
                .when(profileService)
                .validateProfileAccess(profile);

        when(fileStorageService.store(file, "ali"))
                .thenReturn("newMongo");


        profileService.uploadOrUpdateProfilePicture(1L, file, "ali");

        verify(fileStorageService)
                .deleteById("old123");

        verify(attachmentRepository)
                .delete(oldAttachment);
    }

    @Test
    void uploadOrUpdateProfilePicture_shouldThrowFileStorageException()
            throws Exception {

        Profile profile = new Profile();

        User user = new User();
        user.setUsername("ali");
        profile.setUser(user);

        MultipartFile file = mock(MultipartFile.class);

        when(profileRepository.findById(1L))
                .thenReturn(Optional.of(profile));

        doNothing()
                .when(profileService)
                .validateProfileAccess(profile);


        when(fileStorageService.store(any(), any()))
                .thenThrow(IOException.class);


        assertThrows(
                FileStorageException.class,
                () -> profileService.uploadOrUpdateProfilePicture(
                        1L,
                        file,
                        "ali"
                )
        );
    }
}
