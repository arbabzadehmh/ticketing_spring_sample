package ir.controller.web;

import ir.dto.SectionListDto;
import ir.service.SectionService;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(required = false) Boolean fragment,
            Model model
    ) {

        if (size <= 0) size = 10;

        Sort sort = Sort.by("title").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<SectionListDto> sections = sectionService.findAll(pageable);

        model.addAttribute("sections", sections);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", sections.getTotalPages());

        return fragment != null && fragment ?
                "fragments/section-fragments/sections-table :: sections-table" : "section";
    }

}