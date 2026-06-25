package ir.controller;

import ir.config.SecurityConfig;
import ir.controller.exception.ExceptionWrapper;
import ir.controller.web.GlobalModelAttribute;
import ir.controller.web.PermissionController;
import ir.model.entity.Permission;
import ir.service.PermissionService;
import ir.service.impl.CustomUserDetailsService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(PermissionController.class)
@Import(SecurityConfig.class)
public class PermissionControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PermissionService permissionService;

    @MockitoBean
    MessageSource messageSource;

    @MockitoBean
    ExceptionWrapper exceptionWrapper;

    @MockitoBean
    GlobalModelAttribute globalModelAttribute;

    @MockitoBean
    CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldReturnPermissionPage() throws Exception {

        Page<Permission> page =
                new PageImpl<>(
                        List.of(new Permission()),
                        PageRequest.of(0, 10),
                        1
                );

        when(permissionService.findAll(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                        get("/permissions")
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("permission"))
                .andExpect(model().attributeExists("permissions"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1));

        verify(permissionService)
                .findAll(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldReturnFragmentView() throws Exception {

        Page<Permission> page =
                new PageImpl<>(List.of());

        when(permissionService.findAll(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                        get("/permissions")
                                .param("fragment", "true")
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(view().name(
                        "fragments/permission-fragments/permissions-table :: permissions-table"
                ));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void shouldUseDefaultSizeWhenSizeIsZero() throws Exception {

        Page<Permission> page = Page.empty();

        when(permissionService.findAll(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(
                        get("/permissions")
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
    @WithMockUser(roles = {"USER"})
    void shouldRejectUserWithoutRequiredRole() throws Exception {

        mockMvc.perform(
                        get("/permissions")
                                .secure(true)
                )
                .andExpect(status().isForbidden());
    }
}
