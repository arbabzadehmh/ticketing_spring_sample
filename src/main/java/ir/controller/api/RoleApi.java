package ir.controller.api;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rest/roles")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class RoleApi {

    private final RoleService roleService;
    private final MessageSource messageSource;

    public RoleApi(RoleService roleService, MessageSource messageSource) {
        this.roleService = roleService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public ResponseEntity<?> getAllRoles() {
        List<String> roles = roleService.findAll()
                .stream()
                .map(role -> role.getName())
                .collect(Collectors.toList());
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllRolesForTable(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchRoleName
    ) {

        if (size <= 0) size = 10;

        Sort sort = Sort.by("name").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Role> roles;
        if (searchRoleName != null && !searchRoleName.isEmpty()) {
            roles = roleService.findByNameContaining(searchRoleName, pageable);
        } else {
            roles = roleService.findAll(pageable);
        }

        return ResponseEntity.ok(roles);
    }

    @PostMapping
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
    public ResponseEntity<?> updateRole(
            @PathVariable String name,
            @Valid @RequestBody Role role,
            BindingResult bindingResult,
            Locale locale
    ) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        roleService.update(name, role);

        String message = messageSource.getMessage("roles.edit.success", null, locale);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<?> deleteRole(@PathVariable String name, Locale locale) {
        roleService.deleteByName(name);
        String message = messageSource.getMessage("roles.delete.success", null, locale);
        return ResponseEntity.ok(Map.of("message", message));
    }
}
