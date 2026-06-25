package ir.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.config.SecurityConfig;
import ir.controller.api.ProfileApi;
import ir.controller.exception.ExceptionWrapper;
import ir.controller.web.GlobalModelAttribute;
import ir.dto.ForgotPasswordDto;
import ir.dto.ProfileUserDto;
import ir.dto.ResetPasswordDto;
import ir.model.entity.PasswordResetToken;
import ir.model.entity.Profile;
import ir.model.entity.User;
import ir.model.enums.FileType;
import ir.service.UserService;
import ir.service.impl.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileApi.class)
@Import(SecurityConfig.class)
public class ProfileApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProfileService profileService;

    @MockitoBean
    MessageSource messageSource;

    @MockitoBean
    UserService userService;

    @MockitoBean
    EntityLockService lockService;

    @MockitoBean
    PasswordResetService resetService;

    @MockitoBean
    AuthService authService;

    @MockitoBean
    ExceptionWrapper exceptionWrapper;

    @MockitoBean
    GlobalModelAttribute globalModelAttribute;

    @MockitoBean
    CustomUserDetailsService customUserDetailsService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnProfiles() throws Exception {

        Profile profile = new Profile();

        Page<Profile> page =
                new PageImpl<>(List.of(profile),
                        PageRequest.of(0, 10),
                        1
                );

        when(profileService.findAll(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                        get("/rest/profiles")
                                .secure(true)
                )
                .andExpect(status().isOk());

        verify(profileService)
                .findAll(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSearchByLastName() throws Exception {

        Profile profile = new Profile();

        Page<Profile> page =
                new PageImpl<>(List.of(profile),
                        PageRequest.of(0, 10),
                        1
                );

        when(profileService.findByLastNameLike(
                eq("Ahmadi"),
                any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                        get("/rest/profiles")
                                .param("lastName", "Ahmadi")
                                .secure(true)
                )
                .andExpect(status().isOk());

        verify(profileService)
                .findByLastNameLike(
                        eq("Ahmadi"),
                        any(Pageable.class)
                );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSearchByUsername() throws Exception {

        Profile profile = new Profile();

        Page<Profile> page =
                new PageImpl<>(List.of(profile),
                        PageRequest.of(0, 10),
                        1
                );

        when(profileService.findByUserUsernameLike(
                eq("ali"),
                any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                        get("/rest/profiles")
                                .param("username", "ali")
                                .secure(true)
                )
                .andExpect(status().isOk());

        verify(profileService)
                .findByUserUsernameLike(
                        eq("ali"),
                        any(Pageable.class)
                );
    }

    @Test
    void shouldRegisterProfile() throws Exception {

        Profile profile = new Profile();
        profile.setId(1L);

        ProfileUserDto dto = new ProfileUserDto();
        dto.setUsername("ali_123");
        dto.setFirstName("ali");
        dto.setLastName("ahmadi");
        dto.setPassword("123456");

        when(profileService.createProfileByCustomer(any()))
                .thenReturn(profile);

        when(messageSource.getMessage(
                eq("profiles.create.success"),
                any(),
                any(Locale.class)))
                .thenReturn("created");

        mockMvc.perform(
                        post("/rest/profiles/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.message").value("created"));
    }

    @Test
    void shouldRequestPasswordReset() throws Exception {

        ForgotPasswordDto dto = new ForgotPasswordDto();

        dto.setUsername("ali");

        when(messageSource.getMessage(
                eq("users.password.reset.email.sent"),
                any(),
                any(Locale.class)))
                .thenReturn("sent");

        mockMvc.perform(
                        post("/rest/profiles/forgot-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("sent"));

        verify(authService)
                .requestReset("ali");
    }

    @Test
    void shouldResetPassword() throws Exception {

        ResetPasswordDto dto = new ResetPasswordDto();

        dto.setToken("token123");
        dto.setNewPassword("123456");

        PasswordResetToken token = new PasswordResetToken();

        token.setUsername("ali");

        when(resetService.validateToken("token123"))
                .thenReturn(token);

        when(messageSource.getMessage(
                eq("users.password.reset.success"),
                any(),
                any(Locale.class)))
                .thenReturn("reset");

        mockMvc.perform(
                        post("/rest/profiles/reset-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ali"))
                .andExpect(jsonPath("$.message").value("reset"));

        verify(userService)
                .changePassword("ali", "123456");

        verify(resetService)
                .markAsUsed(token);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateProfileByAdmin() throws Exception {

        Profile profile = new Profile();
        profile.setId(5L);

        ProfileUserDto dto = new ProfileUserDto();
        dto.setUsername("ali_123");
        dto.setFirstName("ali");
        dto.setLastName("ahmadi");
        dto.setPassword("123456");

        when(profileService.createProfileByAdmin(any()))
                .thenReturn(profile);

        when(messageSource.getMessage(
                eq("profiles.create.success"),
                any(),
                any(Locale.class)))
                .thenReturn("created");

        mockMvc.perform(
                        post("/rest/profiles/create-profile")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    @WithMockUser(username = "ali")
    void shouldStartEditProfile() throws Exception {

        Profile profile = new Profile();

        when(profileService.findById(1L))
                .thenReturn(profile);

        mockMvc.perform(
                        post("/rest/profiles/1/edit-start")
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk());

        verify(lockService)
                .lock("profile", 1L, "ali");
    }

    @Test
    @WithMockUser(username = "ali")
    void shouldStopEditProfile() throws Exception {

        Profile profile = new Profile();

        when(profileService.findById(1L))
                .thenReturn(profile);

        mockMvc.perform(
                        post("/rest/profiles/1/edit-stop")
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk());

        verify(lockService)
                .unlock("profile", 1L, "ali");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateProfile() throws Exception {

        ProfileUserDto dto = new ProfileUserDto();
        dto.setUsername("ali_123");
        dto.setFirstName("ali");
        dto.setLastName("ahmadi");
        dto.setPassword("123456");

        when(messageSource.getMessage(
                eq("profiles.edit.success"),
                any(),
                any(Locale.class)))
                .thenReturn("updated");

        mockMvc.perform(
                        put("/rest/profiles/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("updated"));

        verify(profileService)
                .updateProfile(
                        any(ProfileUserDto.class),
                        eq(1L),
                        eq(true)
                );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteProfile() throws Exception {

        User user = new User();
        user.setUsername("ali");

        Profile profile = new Profile();
        profile.setUser(user);

        when(profileService.findById(1L))
                .thenReturn(profile);

        when(messageSource.getMessage(
                eq("profiles.delete.success"),
                any(),
                any(Locale.class)))
                .thenReturn("deleted");

        mockMvc.perform(
                        delete("/rest/profiles/1")
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("deleted"));

        verify(profileService)
                .deleteById(1L);
    }

    @Test
    @WithMockUser(username = "ali")
    void shouldUploadPicture() throws Exception {

        Profile profile = new Profile();

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "image.jpg",
                        MediaType.IMAGE_JPEG_VALUE,
                        "test".getBytes()
                );

        when(profileService.uploadOrUpdateProfilePicture(
                eq(1L),
                any(MultipartFile.class),
                eq("ali")))
                .thenReturn(profile);

        mockMvc.perform(
                        multipart("/rest/profiles/1/picture")
                                .file(file)
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldDeletePicture() throws Exception {

        Profile profile = new Profile();
        profile.setVersion(3L);

        when(profileService.deleteProfilePicture(1L))
                .thenReturn(profile);

        when(messageSource.getMessage(
                eq("profiles.picture.delete.success"),
                any(),
                any(Locale.class)))
                .thenReturn("deleted");

        mockMvc.perform(
                        delete("/rest/profiles/1/picture")
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("deleted"))
                .andExpect(jsonPath("$.version").value(3));
    }

    @Test
    @WithMockUser
    void shouldGetPicture() throws Exception {

        byte[] bytes = "image".getBytes();

        when(profileService.getProfilePictureBytes(1L))
                .thenReturn(org.springframework.data.util.Pair.of(
                        bytes,
                        FileType.JPG
                ));

        mockMvc.perform(
                        get("/rest/profiles/1/picture")
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(content().bytes(bytes))
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
    }
}
