package ir.dto;

import ir.model.entity.Building;
import jakarta.validation.Valid;
import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@ToString
public class BuildingCreateRequest {
    @Valid
    private Building building;

    @Valid
    private AddressDto addressDto;

}
