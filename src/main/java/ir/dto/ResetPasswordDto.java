package ir.dto;

import ir.validation.OnCreate;
import ir.validation.OnUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ResetPasswordDto {

    @NotBlank
    private String token;

    @NotBlank(message = "{validation.password}", groups = OnCreate.class)
    @Pattern(regexp = "^[a-zA-Z\\d@_]{3,15}$", message = "{validation.passwordPattern}", groups = {OnCreate.class, OnUpdate.class})
    private String newPassword;
}
