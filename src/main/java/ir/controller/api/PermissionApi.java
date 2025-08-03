package ir.controller.api;

import ir.service.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rest/permissions")
public class PermissionApi {

    private final PermissionService permissionService;

    public PermissionApi(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    @ResponseBody
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<?> getAllPermissions() {
        List<String> permissions = permissionService.findAll()
                .stream()
                .map(permission -> permission.getPermissionName())
                .collect(Collectors.toList());
        return ResponseEntity.ok(permissions);
    }

}
