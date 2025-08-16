package ir.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;


@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@ToString
public class TicketCreateDto {

    @NotBlank(message = "{validation.title}")
    @Size(max = 100, message = "{validation.titleSize}")
    private String title;

    @NotBlank(message = "{validation.content}")
    private String content;

    private Long sectionId;

    private String customerUsername;
}
