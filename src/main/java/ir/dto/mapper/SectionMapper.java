package ir.dto.mapper;

import ir.dto.SectionDto;
import ir.model.entity.Section;
import org.springframework.stereotype.Component;

@Component
public class SectionMapper {

    public SectionDto toDto(Section s) {

        boolean validBuilding =
                s.getBuilding() != null &&
                        !s.getBuilding().isDeleted();

        return new SectionDto(
                s.getId(),
                s.getTitle(),
                s.getParentSection() != null ? s.getParentSection().getId() : null,
                s.getParentSection() != null ? s.getParentSection().getTitle() : null,
                validBuilding ? s.getBuilding().getId() : null,
                validBuilding ? s.getBuilding().getTitle() : null,
                s.getVersion()
        );
    }

}
