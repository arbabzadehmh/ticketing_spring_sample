package ir.controller.web;

import ir.dto.BuildingTableDto;
import ir.service.BuildingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/buildings")
public class BuildingController {

    private final BuildingService buildingService;

    public BuildingController(BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    @GetMapping
    public String buildingsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean fragment,
            Model model
    ) {

        if (size <= 0) size = 10;

        Sort sort = Sort.by("title").ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<BuildingTableDto> buildings = buildingService.findAllForTable(pageable, null);

        model.addAttribute("buildings", buildings);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", buildings.getTotalPages());

        return fragment != null && fragment ?
                "fragments/building-fragments/buildings-table :: buildings-table" : "building";
    }
}
