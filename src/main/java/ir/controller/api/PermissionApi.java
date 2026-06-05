package ir.controller.api;

import ir.controller.exception.ValidationException;
import ir.model.entity.Permission;
import ir.service.PermissionService;
import ir.service.impl.EntityLockService;
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

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rest/permissions")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class PermissionApi {

    private final PermissionService permissionService;
    private final MessageSource messageSource;
    private final EntityLockService lockService;

    public PermissionApi(PermissionService permissionService, MessageSource messageSource, EntityLockService lockService) {
        this.permissionService = permissionService;
        this.messageSource = messageSource;
        this.lockService = lockService;
    }

    @GetMapping
    public ResponseEntity<?> getAllPermissions() {
        List<String> permissions = permissionService.findAll()
                .stream()
                .map(permission -> permission.getPermissionName())
                .collect(Collectors.toList());
        return ResponseEntity.ok(permissions);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllPermissionsForTable(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchPermissionName
    ) {

        if (size <= 0) size = 10;

        Sort sort = Sort.by("permissionName").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Permission> permissions;
        if (searchPermissionName != null && !searchPermissionName.isEmpty()) {
            permissions = permissionService.findByPermissionNameContaining(searchPermissionName, pageable);
        } else {
            permissions = permissionService.findAll(pageable);
        }

        return ResponseEntity.ok(permissions);
    }

    @PostMapping
    public ResponseEntity<?> savePermission(
            @Valid @RequestBody Permission permission,
            BindingResult bindingResult,
            Locale locale
    ){

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            throw new ValidationException(errors);
        }

        permissionService.save(permission);

        String message = messageSource.getMessage("permissions.create.success", null, locale);

        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/{id}/edit-start")
    public ResponseEntity<?> startPermissionEdit(@PathVariable Long id,
                                               Principal principal) {

        lockService.lock("permission", id, principal.getName());
        return ResponseEntity.ok(Map.of("message", "locked"));
    }

    @PostMapping("/{id}/edit-stop")
    public void stopPermissionEdit(@PathVariable Long id,
                                 Principal principal) {

        lockService.unlock("permission", id, principal.getName());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody Permission permission,
            BindingResult bindingResult,
            Locale locale
    ){
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            throw new ValidationException(errors);
        }

        permissionService.update(id, permission);

        String message = messageSource.getMessage("permissions.edit.success", null, locale);

        return ResponseEntity.ok(Map.of("message", message));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePermission(@PathVariable Long id, Locale locale){
        permissionService.deleteById(id);
        String message = messageSource.getMessage("permissions.delete.success", null, locale);
        return ResponseEntity.ok(Map.of("message", message));
    }

}
