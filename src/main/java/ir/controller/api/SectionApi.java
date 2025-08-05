package ir.controller.api;

import ir.model.entity.Section;
import ir.repository.SectionRepository;
import ir.service.SectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rest/sections")
public class SectionApi {

    private final SectionService sectionService;

    public SectionApi(SectionRepository sectionRepository, SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @GetMapping
    @ResponseBody
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


}
