package ir.controller;

import ir.config.SecurityConfig;
import ir.controller.api.PermissionApi;
import ir.controller.exception.ExceptionWrapper;
import ir.controller.web.GlobalModelAttribute;
import ir.model.entity.Permission;
import ir.service.PermissionService;
import ir.service.impl.CustomUserDetailsService;
import ir.service.impl.EntityLockService;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(PermissionApi.class)
@Import(SecurityConfig.class)
public class PermissionApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PermissionService permissionService;

    @MockitoBean
    MessageSource messageSource;

    @MockitoBean
    EntityLockService lockService;

    @MockitoBean
    ExceptionWrapper exceptionWrapper;

    @MockitoBean
    GlobalModelAttribute globalModelAttribute;

    @MockitoBean
    CustomUserDetailsService customUserDetailsService;


    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnAllPermissions() throws Exception {

        Permission p1 = new Permission();
        p1.setPermissionName("USER_CREATE");

        Permission p2 = new Permission();
        p2.setPermissionName("USER_DELETE");

        when(permissionService.findAll())
                .thenReturn(List.of(p1, p2));

        mockMvc.perform(
                        get("/rest/permissions")
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("USER_CREATE"))
                .andExpect(jsonPath("$[1]").value("USER_DELETE"));

        verify(permissionService).findAll();
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldReturnPermissionsPage() throws Exception {

        Page<Permission> page =
                new PageImpl<>(
                        List.of(new Permission()),
                        PageRequest.of(0, 10),
                        1
                );

        when(permissionService.findAll(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                        get("/rest/permissions/get-all")
                                .secure(true)
                )
                .andExpect(status().isOk());

        verify(permissionService)
                .findAll(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSearchPermissions() throws Exception {

        Permission permission = new Permission();
        permission.setId(1L);
        permission.setPermissionName("USER_READ");

        Page<Permission> page =
                new PageImpl<>(
                        List.of(permission),
                        PageRequest.of(0, 10),
                        1
                );

        when(permissionService.findByPermissionNameContaining(
                eq("USER"),
                any(Pageable.class)))
                .thenReturn(page);

        when(exceptionWrapper.getMessage(any(), any()))
                .thenReturn("mock error");

        mockMvc.perform(
                        get("/rest/permissions/get-all")
                                .param("searchPermissionName", "USER")
                                .secure(true)
                )
                .andExpect(status().isOk());

        verify(permissionService)
                .findByPermissionNameContaining(
                        eq("USER"),
                        any(Pageable.class)
                );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUseDefaultSizeWhenSizeIsZero() throws Exception {

        Permission permission = new Permission();
        permission.setId(1L);
        permission.setPermissionName("USER_READ");

        Page<Permission> page =
                new PageImpl<>(
                        List.of(permission),
                        PageRequest.of(0, 10),
                        1
                );

        when(permissionService.findAll(any(Pageable.class)))
                .thenReturn(page);

        when(exceptionWrapper.getMessage(any(), any()))
                .thenReturn("mock error");

        mockMvc.perform(
                        get("/rest/permissions/get-all")
                                .param("size", "0")
                                .secure(true)
                )
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(permissionService)
                .findAll(captor.capture());

        assertEquals(
                10,
                captor.getValue().getPageSize()
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSavePermission() throws Exception {

        when(messageSource.getMessage(
                eq("permissions.create.success"),
                isNull(),
                any(Locale.class)
        )).thenReturn("Created");

        mockMvc.perform(
                        post("/rest/permissions")
                                .with(csrf())
                                .secure(true)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "permissionName":"USER_CREATE"
                                    }
                                    """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Created"));

        verify(permissionService)
                .save(any(Permission.class));
    }

    @Test
    @WithMockUser(
            username = "ali",
            roles = "ADMIN"
    )
    void shouldLockPermission() throws Exception {

        mockMvc.perform(
                        post("/rest/permissions/1/edit-start")
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("locked"));

        verify(lockService)
                .lock(
                        "permission",
                        1L,
                        "ali"
                );
    }

    @Test
    @WithMockUser(
            username = "ali",
            roles = "ADMIN"
    )
    void shouldUnlockPermission() throws Exception {

        mockMvc.perform(
                        post("/rest/permissions/1/edit-stop")
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk());

        verify(lockService)
                .unlock(
                        "permission",
                        1L,
                        "ali"
                );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdatePermission() throws Exception {

        when(messageSource.getMessage(
                eq("permissions.edit.success"),
                isNull(),
                any(Locale.class)
        )).thenReturn("Updated");

        mockMvc.perform(
                        put("/rest/permissions/1")
                                .with(csrf())
                                .secure(true)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "permissionName":"USER_EDIT"
                                    }
                                    """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Updated"));

        verify(permissionService)
                .update(
                        eq(1L),
                        any(Permission.class)
                );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeletePermission() throws Exception {

        when(messageSource.getMessage(
                eq("permissions.delete.success"),
                isNull(),
                any(Locale.class)
        )).thenReturn("Deleted");

        mockMvc.perform(
                        delete("/rest/permissions/1")
                                .with(csrf())
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Deleted"));

        verify(permissionService)
                .deleteById(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldRejectUserWithoutRole() throws Exception {

        mockMvc.perform(
                        get("/rest/permissions")
                                .secure(true)
                )
                .andExpect(status().isForbidden());
    }
}
