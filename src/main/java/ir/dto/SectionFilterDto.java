package ir.dto;

import java.io.Serial;
import java.io.Serializable;

public record SectionFilterDto (Long id, String title) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

}
