package ir.controller;


import ir.config.SecurityConfig;
import ir.controller.exception.ExceptionWrapper;
import ir.controller.web.GlobalModelAttribute;
import ir.controller.web.ProfileController;
import ir.dto.mapper.ProfileMapper;
import ir.model.entity.PasswordResetToken;
import ir.model.entity.Profile;
import ir.model.entity.User;
import ir.service.RoleService;
import ir.service.UserService;
import ir.service.impl.CustomUserDetailsService;
import ir.service.impl.FileStorageService;
import ir.service.impl.PasswordResetService;
import ir.service.impl.ProfileService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(ProfileController.class)
@Import(SecurityConfig.class)
public class ProfileControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProfileService profileService;

    @MockitoBean
    UserService userService;

    @MockitoBean
    RoleService roleService;

    @MockitoBean
    ProfileMapper profileMapper;

    @MockitoBean
    FileStorageService fileStorageService;

    @MockitoBean
    MessageSource messageSource;

    @MockitoBean
    PasswordResetService resetService;

    @MockitoBean
    GlobalModelAttribute globalModelAttribute;

    @MockitoBean
    CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    ExceptionWrapper exceptionWrapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowProfilesListForAdmin() throws Exception {

        User user = new User();
        user.setUsername("ali");

        Profile profile = new Profile();
        profile.setUser(user);

        Page<Profile> page =
                new PageImpl<>(
                        List.of(profile),
                        PageRequest.of(0,10),
                        1
                );

        when(profileService.findAll(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/profiles").secure(true))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("profiles"))
                .andExpect(model().attribute("isAdmin", true));

        verify(profileService)
                .findAll(any(Pageable.class));
    }

    @Test
    @WithMockUser(
            username = "ali",
            roles = "CUSTOMER"
    )
    void shouldShowOwnProfileForCustomer() throws Exception {

        User user = new User();
        user.setUsername("ali");

        Profile profile = new Profile();
        profile.setUser(user);


        when(profileService.findByUsername("ali"))
                .thenReturn(profile);

        mockMvc.perform(get("/profiles").secure(true))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().attribute("isAdmin", false));

        verify(profileService)
                .findByUsername("ali");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUseDefaultSizeWhenSizeIsZero() throws Exception {

        User user = new User();
        user.setUsername("ali");

        Profile profile = new Profile();
        profile.setUser(user);

        Page<Profile> page =
                new PageImpl<>(
                        List.of(profile),
                        PageRequest.of(0,10),
                        1
                );

        when(profileService.findAll(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                        get("/profiles")
                                .param("size","0")
                                .secure(true)
                )
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(profileService)
                .findAll(captor.capture());

        assertEquals(
                10,
                captor.getValue().getPageSize()
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUseDefaultSortWhenSortByInvalid() throws Exception {

        User user = new User();
        user.setUsername("ali");

        Profile profile = new Profile();
        profile.setUser(user);

        Page<Profile> page =
                new PageImpl<>(
                        List.of(profile),
                        PageRequest.of(0,10),
                        1
                );

        when(profileService.findAll(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                        get("/profiles")
                                .param("sortBy","invalid")
                                .secure(true)
                )
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(profileService)
                .findAll(captor.capture());

        assertEquals(
                "firstName",
                captor.getValue()
                        .getSort()
                        .iterator()
                        .next()
                        .getProperty()
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnFragmentView() throws Exception {

        User user = new User();
        user.setUsername("ali");

        Profile profile = new Profile();
        profile.setUser(user);

        Page<Profile> page =
                new PageImpl<>(
                        List.of(profile),
                        PageRequest.of(0,10),
                        1
                );

        when(profileService.findAll(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                        get("/profiles")
                                .param("fragment","true")
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(view().name(
                        "fragments/profile-fragments/profiles-table :: profiles-table"
                ));
    }

    @Test
    @WithMockUser
    void shouldReturnProfileCard() throws Exception {

        User user = new User();
        user.setUsername("ali");

        Profile profile = new Profile();
        profile.setUser(user);

        when(profileService.findById(1L))
                .thenReturn(profile);

        mockMvc.perform(
                        get("/profiles/1/card")
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(view().name(
                        "fragments/profile-fragments/profile-card :: profile-card"
                ))
                .andExpect(model().attributeExists("profile"));

        verify(profileService)
                .validateProfileAccess(profile);
    }

    @Test
    void shouldShowForgotPasswordPage() throws Exception {

        mockMvc.perform(
                        get("/profiles/forgot-password")
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("forgot-password"));
    }

    @Test
    void shouldShowResetPasswordPage() throws Exception {

        PasswordResetToken token = new PasswordResetToken();

        when(resetService.validateToken("token123"))
                .thenReturn(token);

        mockMvc.perform(
                        get("/profiles/reset-password")
                                .param("token","token123")
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("reset-password"))
                .andExpect(model().attribute("token","token123"));

        verify(resetService)
                .validateToken("token123");
    }
}
