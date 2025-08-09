package ir.controller.web;


import ir.controller.exception.ValidationException;
import ir.model.entity.Section;
import ir.service.SectionService;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/sections")
public class SectionController {
    private final SectionService sectionService;
    private final MessageSource messageSource;

    public SectionController(SectionService sectionService, MessageSource messageSource) {
        this.sectionService = sectionService;
        this.messageSource = messageSource;
    }

    @GetMapping
    public String sectionsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sectionTitle,
            @RequestParam(required = false) String parentSectionTitle,
            @RequestParam(required = false) Boolean fragment,
            Model model) {

        if (size <= 0) size = 10;

        // ایجاد صفحه‌بندی با مرتب‌سازی
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
        model.addAttribute("sections", sections);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", sections.getTotalPages());

        return fragment != null && fragment ?
                "fragments/section-fragments/sections-table :: sections-table" :
                "section";

    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @ResponseBody
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
    @ResponseBody
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
    @ResponseBody
    public ResponseEntity<?> deleteSection(@PathVariable Long id, Locale locale) {
        sectionService.deleteById(id);
        String message = messageSource.getMessage("sections.delete.success", null, locale);
        return ResponseEntity.ok(Map.of("message", message));
    }

}