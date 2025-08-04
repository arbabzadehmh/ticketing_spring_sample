package ir.controller.web;

import ir.controller.exception.ValidationException;
import ir.model.entity.Role;
import ir.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


@Controller
@RequestMapping("/roles")
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
        // اعتبارسنجی پارامترها
        if (size <= 0) size = 10;

        // ایجاد صفحه‌بندی با مرتب‌سازی
        Sort sort = Sort.by("name").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Role> roles = roleService.findAll(pageable);
        model.addAttribute("roles", roles);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", roles.getTotalPages());

        return fragment != null && fragment ?
                "fragments/role-fragments/roles-table :: roles-table" :
                "role";

    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<?> saveRole(
            @Valid @RequestBody Role role,
            BindingResult bindingResult,
            Locale locale
    ) {

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            throw new ValidationException(errors);
        }

        roleService.save(role);

        String message = messageSource.getMessage("roles.create.success", null, locale);

        return ResponseEntity.ok(Map.of("message", message));
    }

    @PutMapping("/{name}")
    @ResponseBody
    public ResponseEntity<?> updateRole(
            @PathVariable String name,
            @Valid @RequestBody Role role,
            BindingResult bindingResult,
            Locale locale
    ){
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        roleService.update(name,role);

        String message = messageSource.getMessage("roles.edit.success", null, locale);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @DeleteMapping("/{name}")
    @ResponseBody
    public ResponseEntity<?> deleteRole(@PathVariable String name, Locale locale) {
        roleService.deleteByName(name);
        String message = messageSource.getMessage("roles.delete.success", null, locale);
        return ResponseEntity.ok(Map.of("message", message));
    }

}
