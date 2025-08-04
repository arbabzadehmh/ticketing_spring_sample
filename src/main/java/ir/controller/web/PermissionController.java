package ir.controller.web;

import ir.controller.exception.ValidationException;
import ir.model.entity.Permission;
import ir.service.PermissionService;
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
@RequestMapping("/permissions")
public class PermissionController {

    private final PermissionService permissionService;
    private final MessageSource messageSource;

    public PermissionController(PermissionService permissionService, MessageSource messageSource) {
        this.permissionService = permissionService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String permissionsList(
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

    @PostMapping
    @ResponseBody
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

    @PutMapping("/{id}")
    @ResponseBody
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
    @ResponseBody
    public ResponseEntity<?> deletePermission(@PathVariable Long id, Locale locale){
        permissionService.deleteById(id);
        String message = messageSource.getMessage("permissions.delete.success", null, locale);
        return ResponseEntity.ok(Map.of("message", message));
    }
}
