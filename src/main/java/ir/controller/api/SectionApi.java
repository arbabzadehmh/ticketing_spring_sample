package ir.controller.api;

import ir.controller.exception.ValidationException;
import ir.dto.SectionDto;
import ir.model.entity.Section;
import ir.repository.SectionRepository;
import ir.service.SectionService;
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

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rest/sections")
public class SectionApi {

    private final SectionService sectionService;
    private final MessageSource messageSource;

    public SectionApi(SectionService sectionService, MessageSource messageSource) {
        this.sectionService = sectionService;
        this.messageSource = messageSource;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<Section>> getAvailableParents(@RequestParam(required = false) Long id) {
        List<Section> allSections = sectionService.findAll();

        if (id != null) {
            Section section = sectionService.findById(id);
            Set<Long> excludedIds = new HashSet<>();
            excludedIds.add(section.getId());
            collectChildIds(section, excludedIds);

            allSections = allSections.stream()
                    .filter(s -> !excludedIds.contains(s.getId()))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(allSections);
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

        Page<Section> sections;
        if (sectionTitle != null && !sectionTitle.isEmpty()) {
            sections = sectionService.findByTitleContaining(sectionTitle, pageable);
        } else if (parentSectionTitle != null && !parentSectionTitle.isEmpty()) {
            sections = sectionService.findByParentSectionTitleContaining(parentSectionTitle, pageable);
        } else {
            sections = sectionService.findAll(pageable);
        }

        return ResponseEntity.ok(sections);
    }

//    @GetMapping("/dto-for-building")
//    public List<SectionDto> getAllForBuilding() {
//        return sectionService.findAll()
//                .stream()
//                .map(s -> new SectionDto(s.getId(), s.getTitle()))
//                .toList();
//    }


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
