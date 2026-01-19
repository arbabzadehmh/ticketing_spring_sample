package ir.service;

import ir.dto.AddressDto;
import ir.dto.BuildingTableDto;
import ir.model.entity.Building;
import ir.model.entity.Section;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BuildingService {
    Building save(Building building, AddressDto addressDto);
    Building edit(Building building, AddressDto addressDto);
    void deleteById(Long id);
    Page<BuildingTableDto> findAllForTable(Pageable pageable, String searchTitle);
    List<Building> findAll();
    Page<Building> findAll(Pageable pageable);
    Page<Building> findByTitleContaining(String title, Pageable pageable);
}
