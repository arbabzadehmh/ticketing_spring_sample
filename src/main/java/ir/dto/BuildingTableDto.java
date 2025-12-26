package ir.dto;

import java.util.List;

public record BuildingTableDto(
        Long id,
        String title,
        List<String> phones,
        List<String> sections,
        String fullAddress
)
{}
