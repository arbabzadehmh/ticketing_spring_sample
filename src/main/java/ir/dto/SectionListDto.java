package ir.dto;

import java.io.Serial;
import java.io.Serializable;

public record SectionListDto (
        Long id,
        String title,

        Long parentSectionId,
        String parentSectionTitle,

        Long buildingId,
        String buildingTitle,

        Long version
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
