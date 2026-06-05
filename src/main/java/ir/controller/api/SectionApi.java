package ir.controller.api;

import ir.controller.exception.ValidationException;

import ir.dto.SectionDto;
import ir.dto.SectionListDto;
import ir.dto.mapper.SectionMapper;
import ir.model.entity.Section;
import ir.service.SectionService;
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
import java.util.*;

@RestController
@RequestMapping("/rest/sections")
public class SectionApi {

    private final SectionService sectionService;
    private final MessageSource messageSource;
    private final EntityLockService lockService;
    private final SectionMapper sectionMapper;

    public SectionApi(SectionService sectionService, MessageSource messageSource, EntityLockService lockService, SectionMapper sectionMapper) {
        this.sectionService = sectionService;
        this.messageSource = messageSource;
        this.lockService = lockService;
        this.sectionMapper = sectionMapper;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<SectionDto>> getAvailableParents(@RequestParam(required = false) Long id) {

        List<Section> allSections = sectionService.findAll();

        if (id != null) {
            Section section = sectionService.findById(id);

            Set<Long> excludedIds = new HashSet<>();
            excludedIds.add(section.getId());
            collectChildIds(section, excludedIds);

            allSections = allSections.stream()
                    .filter(s -> !excludedIds.contains(s.getId()))
                    .toList();
        }

        List<SectionDto> result = allSections.stream()
                .map(sectionMapper::toDto)
                .toList();

        return ResponseEntity.ok(result);
    }

    private void collectChildIds(Section section, Set<Long> ids) {
        for (Section child : section.getChildSectionList()) {
            ids.add(child.getId());
            collectChildIds(child, ids);
        }
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllSections(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sectionTitle,
            @RequestParam(required = false) String parentSectionTitle
    ) {

        if (size <= 0) size = 10;
        Sort sort = Sort.by("title").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<SectionListDto> sections;
        if (sectionTitle != null && !sectionTitle.isEmpty()) {
            sections = sectionService.findByTitleContaining(sectionTitle, pageable);
        } else if (parentSectionTitle != null && !parentSectionTitle.isEmpty()) {
            sections = sectionService.findByParentSectionTitleContaining(parentSectionTitle, pageable);
        } else {
            sections = sectionService.findAll(pageable);
        }

        return ResponseEntity.ok(sections);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> saveSection(
            @Valid @RequestBody Section section,
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

        sectionService.save(section);

        String message = messageSource.getMessage("sections.create.success", null, locale);

        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/{id}/edit-start")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> startSectionEdit(@PathVariable Long id,
                          Principal principal) {

        lockService.lock("section", id, principal.getName());
        return ResponseEntity.ok(Map.of("message", "locked"));
    }

    @PostMapping("/{id}/edit-stop")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void stopSectionEdit(@PathVariable Long id,
                         Principal principal) {

        lockService.unlock("section", id, principal.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> updateSection(
            @PathVariable Long id,
            @Valid @RequestBody Section section,
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

        sectionService.update(id, section);

        String message = messageSource.getMessage("sections.edit.success", null, locale);
        return ResponseEntity.ok(Map.of("message", message));

    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> deleteSection(@PathVariable Long id, Locale locale) {
        sectionService.deleteById(id);
        String message = messageSource.getMessage("sections.delete.success", null, locale);
        return ResponseEntity.ok(Map.of("message", message));
    }


}
