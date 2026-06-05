package ir.controller.web;

import ir.model.entity.Role;
import ir.service.RoleService;
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
@RequestMapping("/roles")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class RoleController {

    private final RoleService roleService;
    private final MessageSource messageSource;

    public RoleController(RoleService roleService, MessageSource messageSource) {
        this.roleService = roleService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String rolesList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean fragment,
            Model model
    ) {

        if (size <= 0) size = 10;

        Sort sort = Sort.by("name").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Role> roles = roleService.findAll(pageable);

        model.addAttribute("roles", roles);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", roles.getTotalPages());

        return fragment != null && fragment ?
                "fragments/role-fragments/roles-table :: roles-table" : "role";
    }

}
