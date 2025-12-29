package ir.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@ToString
public class AddressDto {

    private Long id;

    @NotBlank(message = "{validation.country}")
    @Pattern(regexp = "^[a-zA-Z\\u0600-\\u06FF\\s]{2,30}$",
            message = "{validation.countryPattern}")
    private String country;

    @NotBlank(message = "{validation.state}")
    @Pattern(regexp = "^[a-zA-Z\\u0600-\\u06FF\\s]{2,30}$",
            message = "{validation.statePattern}")
    private String state;

    @NotBlank(message = "{validation.city}")
    @Pattern(regexp = "^[a-zA-Z\\u0600-\\u06FF\\s]{2,30}$",
            message = "{validation.cityPattern}")
    private String city;

    @Pattern(regexp = "^[a-zA-Z\\u0600-\\u06FF\\s]{2,30}$",
            message = "{validation.villagePattern}")
    private String village;

    @Pattern(regexp = "^[a-zA-Z\\u0600-\\u06FF0-9\\u06F0-\\u06F9\\s]{1,30}$",
            message = "{validation.regionPattern}")
    private String region;

    @NotBlank(message = "{validation.street}")
    @Pattern(regexp = "^[a-zA-Z\\u0600-\\u06FF0-9\\u06F0-\\u06F9()&@$_\\-\\s]{2,150}$",
            message = "{validation.streetPattern}")
    private String street;

    @NotBlank(message = "{validation.plateNumber}")
    @Pattern(regexp = "^[a-zA-Z\\u0600-\\u06FF0-9\\u06F0-\\u06F9&_\\-]{1,5}$",
            message = "{validation.plateNumberPattern}")
    private String platesNumber;

    @Pattern(regexp = "^[a-zA-Z\\u0600-\\u06FF0-9\\u06F0-\\u06F9]{1,5}$",
            message = "{validation.floor}")
    private String floor;

    @Pattern(regexp = "^[0-9\\u06F0-\\u06F9]{1,5}$",
            message = "{validation.unit}")
    private String unit;

    @NotBlank(message = "{validation.postalCode}")
    @Pattern(regexp = "^[0-9\\u06F0-\\u06F9]{10}$",
            message = "{validation.postalCodePattern}")
    private String postalCode;

}
