package ir.controller;

import ir.config.SecurityConfig;
import ir.controller.exception.ExceptionWrapper;
import ir.controller.web.GlobalModelAttribute;
import ir.controller.web.RoleController;
import ir.model.entity.Role;
import ir.service.RoleService;
import ir.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

@WebMvcTest(RoleController.class)
@Import(SecurityConfig.class)
public class RoleControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RoleService roleService;

    @MockitoBean
    MessageSource messageSource;

    @MockitoBean
    GlobalModelAttribute globalModelAttribute;

    @MockitoBean
    CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    ExceptionWrapper exceptionWrapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldShowRolesPage() throws Exception {

        Page<Role> roles = new PageImpl<>(List.of(new Role()));

        when(roleService.findAll(any(Pageable.class)))
                .thenReturn(roles);

        mockMvc.perform(
                        get("/roles")
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("role"))
                .andExpect(model().attributeExists("roles"))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1));

        verify(roleService)
                .findAll(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void shouldShowRolesFragment() throws Exception {

        Page<Role> roles = new PageImpl<>(List.of(new Role()));

        when(roleService.findAll(any(Pageable.class)))
                .thenReturn(roles);

        mockMvc.perform(
                        get("/roles")
                                .param("fragment", "true")
                                .secure(true)
                )
                .andExpect(status().isOk())
                .andExpect(view().name(
                        "fragments/role-fragments/roles-table :: roles-table"
                ));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUseDefaultSizeWhenSizeIsZero() throws Exception {

        Page<Role> roles = new PageImpl<>(List.of(new Role()));

        when(roleService.findAll(any(Pageable.class)))
                .thenReturn(roles);

        mockMvc.perform(
                        get("/roles")
                                .param("size", "0")
                                .secure(true)
                )
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(roleService).findAll(captor.capture());

        assertEquals(10, captor.getValue().getPageSize());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForNormalUser() throws Exception {

        mockMvc.perform(
                        get("/roles")
                                .secure(true)
                )
                .andExpect(status().isForbidden());
    }

}
