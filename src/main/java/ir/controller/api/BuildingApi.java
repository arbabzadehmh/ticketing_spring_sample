package ir.controller.api;

import ir.dto.BuildingCreateRequest;
import ir.dto.BuildingTableDto;
import ir.service.BuildingService;
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
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/rest/buildings")
public class BuildingApi {

    private final BuildingService buildingService;
    private final MessageSource messageSource;
    private final EntityLockService lockService;

    public BuildingApi(BuildingService buildingService, MessageSource messageSource, EntityLockService lockService) {
        this.buildingService = buildingService;
        this.messageSource = messageSource;
        this.lockService = lockService;
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllBuildingsForTable(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchBuildingTitle
    ) {

        if (size <= 0) size = 10;

        Sort sort = Sort.by("title").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<BuildingTableDto> buildings = buildingService.findAllForTable(pageable, searchBuildingTitle);

        return ResponseEntity.ok(buildings);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('BUILDING_CREATE')")
    public ResponseEntity<?> saveBuilding(
            @Valid @RequestBody BuildingCreateRequest buildingCreateRequest,
            BindingResult bindingResult,
            Locale locale
    ) {

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> {
                // از نام واقعی فیلد استفاده کن، حتی اگر nested باشد
                String field = error.getField();
                errors.put(field, error.getDefaultMessage());
            });

            // برگرداندن خطاها به صورت Bad Request
            return ResponseEntity.badRequest().body(errors);
        }

        buildingService.save(buildingCreateRequest.getBuilding(), buildingCreateRequest.getAddressDto());

        String message = messageSource.getMessage("buildings.create.success", null, locale);

        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/{id}/edit-start")
    @PreAuthorize("hasAuthority('BUILDING_EDIT')")
    public ResponseEntity<?> startBuildingEdit(@PathVariable Long id,
                                               Principal principal) {

        lockService.lock("building", id, principal.getName());
        return ResponseEntity.ok(Map.of("message", "locked"));
    }

    @PostMapping("/{id}/edit-stop")
    @PreAuthorize("hasAuthority('BUILDING_EDIT')")
    public void stopBuildingEdit(@PathVariable Long id,
                                 Principal principal) {

        lockService.unlock("building", id, principal.getName());
    }

    @PutMapping
    @PreAuthorize("hasAuthority('BUILDING_EDIT')")
    public ResponseEntity<?> editBuilding(
            @Valid @RequestBody BuildingCreateRequest buildingCreateRequest,
            BindingResult bindingResult,
            Locale locale
    ) {

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> {
                // از نام واقعی فیلد استفاده کن، حتی اگر nested باشد
                String field = error.getField();
                errors.put(field, error.getDefaultMessage());
            });

            // برگرداندن خطاها به صورت Bad Request
            return ResponseEntity.badRequest().body(errors);
        }

        buildingService.edit(buildingCreateRequest.getBuilding(), buildingCreateRequest.getAddressDto());

        String message = messageSource.getMessage("buildings.edit.success", null, locale);

        return ResponseEntity.ok(Map.of("message", message));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BUILDING_DELETE')")
    public ResponseEntity<?> deleteBuilding(@PathVariable Long id, Locale locale) {
        buildingService.deleteById(id);
        String message = messageSource.getMessage("buildings.delete.success", null, locale);
        return ResponseEntity.ok(Map.of("message", message));
    }
}
