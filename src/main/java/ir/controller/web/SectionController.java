package ir.controller.web;

import ir.model.entity.Section;
import ir.service.SectionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sections")
public class SectionController {
    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @GetMapping
    public String loadSectionPage(Model model) {
        model.addAttribute("section", new Section());
        model.addAttribute("sectionList", sectionService.findAllSections());

        return "sections";
    }

    @PostMapping
    public String save(Model model, Section section) {
        sectionService.addSection(section);

        return "redirect:sections";

    }
}