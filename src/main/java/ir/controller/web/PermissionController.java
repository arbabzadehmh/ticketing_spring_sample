package ir.controller.web;

import ir.model.entity.Permission;
import ir.service.PermissionService;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/permissions")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class PermissionController {

    private final PermissionService permissionService;
    private final MessageSource messageSource;

    public PermissionController(PermissionService permissionService, MessageSource messageSource) {
        this.permissionService = permissionService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String listPermissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean fragment,
            Model model
    ) {
        if (size <= 0) size = 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("permissionName").ascending());

        Page<Permission> permissions = permissionService.findAll(pageable);

        model.addAttribute("permissions", permissions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", permissions.getTotalPages());

        return fragment != null && fragment
                ? "fragments/permission-fragments/permissions-table :: permissions-table"
                : "permission";
    }

}
