package ir.dto;

import ir.model.enums.TicketStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@ToString
public class TicketEditDto {

    private TicketStatus status;

    private Long sectionId;
}
