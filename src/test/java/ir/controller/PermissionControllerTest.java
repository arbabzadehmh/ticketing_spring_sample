package ir.controller;

import ir.controller.web.PermissionController;
import ir.model.entity.Permission;
import ir.service.PermissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.*;
import org.springframework.ui.Model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PermissionControllerTest {

    @Mock
    PermissionService permissionService;

    @Mock
    MessageSource messageSource;

    @Mock
    Model model;

    @InjectMocks
    PermissionController controller;

    @Test
    void shouldReturnPermissionPage() {

        Page<Permission> page =
                new PageImpl<>(
                        List.of(new Permission()),
                        PageRequest.of(0, 10),
                        1
                );

        when(permissionService.findAll(any(Pageable.class)))
                .thenReturn(page);

        String view = controller.listPermissions(
                0,
                10,
                null,
                model
        );

        assertEquals("permission", view);

        verify(model)
                .addAttribute("permissions", page);

        verify(model)
                .addAttribute("currentPage", 0);

        verify(model)
                .addAttribute("totalPages", 1);
    }

    @Test
    void shouldReturnFragmentView() {

        Page<Permission> page =
                Page.empty();

        when(permissionService.findAll(any(Pageable.class)))
                .thenReturn(page);

        String view = controller.listPermissions(
                0,
                10,
                true,
                model
        );

        assertEquals(
                "fragments/permission-fragments/permissions-table :: permissions-table",
                view
        );
    }

    @Test
    void shouldUseDefaultSizeWhenSizeIsZero() {

        Page<Permission> page =
                Page.empty();

        when(permissionService.findAll(any(Pageable.class)))
                .thenReturn(page);

        controller.listPermissions(
                0,
                0,
                null,
                model
        );

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
    void shouldSortByPermissionNameAscending() {

        Page<Permission> page =
                Page.empty();

        when(permissionService.findAll(any(Pageable.class)))
                .thenReturn(page);

        controller.listPermissions(
                0,
                10,
                null,
                model
        );

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(permissionService)
                .findAll(captor.capture());

        Pageable pageable = captor.getValue();

        assertEquals(
                "permissionName",
                pageable.getSort()
                        .iterator()
                        .next()
                        .getProperty()
        );

        assertEquals(
                Sort.Direction.ASC,
                pageable.getSort()
                        .iterator()
                        .next()
                        .getDirection()
        );
    }
}
