package ir.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import ir.config.SecurityConfig;
import ir.controller.api.RoleApi;
import ir.controller.exception.ExceptionWrapper;
import ir.controller.exception.GlobalExceptionHandler;
import ir.controller.web.GlobalModelAttribute;
import ir.model.entity.Role;
import ir.service.RoleService;
import ir.service.impl.CustomUserDetailsService;
import ir.service.impl.EntityLockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(RoleApi.class)
@Import(SecurityConfig.class)
public class RoleApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    RoleService roleService;

    @MockitoBean
    MessageSource messageSource;

    @MockitoBean
    EntityLockService lockService;

    @MockitoBean
    GlobalExceptionHandler globalExceptionHandler;

    @MockitoBean
    ExceptionWrapper exceptionWrapper;

    @MockitoBean
    GlobalModelAttribute globalModelAttribute;

    @MockitoBean
    CustomUserDetailsService customUserDetailsService;


    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllRoles() throws Exception {

        Role role1 = new Role();
        role1.setName("ADMIN");

        Role role2 = new Role();
        role2.setName("USER");

        when(roleService.findAll())
                .thenReturn(List.of(role1, role2));

        mockMvc.perform(
                        get("/rest/roles")
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("ADMIN"))
                .andExpect(jsonPath("$[1]").value("USER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetRolesForTable() throws Exception {

        Page<Role> page =
                new PageImpl<>(List.of(new Role()));

        when(roleService.findAll(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                        get("/rest/roles/get-all")
                                .secure(true)
                )
                .andExpect(status().isOk());

        verify(roleService)
                .findAll(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSearchRoles() throws Exception {

        Page<Role> page =
                new PageImpl<>(List.of(new Role()));

        when(roleService.findByNameContaining(
                eq("ADMIN"),
                any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                        get("/rest/roles/get-all")
                                .param("searchRoleName", "ADMIN")
                                .secure(true)
                )
                .andExpect(status().isOk());

        verify(roleService)
                .findByNameContaining(
                        eq("ADMIN"),
                        any(Pageable.class)
                );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSaveRole() throws Exception {

        Role role = new Role();
        role.setName("SUPERVISOR");

        when(messageSource.getMessage(
                eq("roles.create.success"),
                isNull(),
                any(Locale.class)))
                .thenReturn("created");

        mockMvc.perform(
                        post("/rest/roles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(role))
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("created"));

        verify(roleService).save(any(Role.class));
    }

    @Test
    @WithMockUser(
            username = "admin",
            roles = "ADMIN"
    )
    void shouldStartEdit() throws Exception {

        mockMvc.perform(
                        post("/rest/roles/ADMIN/edit-start")
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("locked"));

        verify(lockService)
                .lock("role", "ADMIN", "admin");
    }

    @Test
    @WithMockUser(
            username = "admin",
            roles = "ADMIN"
    )
    void shouldStopEdit() throws Exception {

        mockMvc.perform(
                        post("/rest/roles/ADMIN/edit-stop")
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk());

        verify(lockService)
                .unlock("role", "ADMIN", "admin");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateRole() throws Exception {

        Role role = new Role();
        role.setName("NEW_ROLE");

        when(messageSource.getMessage(
                eq("roles.edit.success"),
                isNull(),
                any(Locale.class)))
                .thenReturn("updated");

        mockMvc.perform(
                        put("/rest/roles/ADMIN")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(role))
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("updated"));

        verify(roleService)
                .update(eq("ADMIN"), any(Role.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteRole() throws Exception {

        when(messageSource.getMessage(
                eq("roles.delete.success"),
                isNull(),
                any(Locale.class)))
                .thenReturn("deleted");

        mockMvc.perform(
                        delete("/rest/roles/ADMIN")
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("deleted"));

        verify(roleService)
                .deleteByName("ADMIN");
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForUser() throws Exception {

        mockMvc.perform(
                        get("/rest/roles")
                                .secure(true)
                )
                .andExpect(status().isForbidden());
    }
}
