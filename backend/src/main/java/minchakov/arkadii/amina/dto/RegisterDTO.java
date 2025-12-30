package minchakov.arkadii.amina.dto;

import jakarta.validation.constraints.NotBlank;
import minchakov.arkadii.amina.validator.UsernameConstraint;
import org.hibernate.validator.constraints.Length;

public record RegisterDTO(
    @NotBlank(message = "Field cannot be blank") @UsernameConstraint String username,

    @NotBlank(message = "Field cannot be blank") @Length(
        min = 64, max = 64, message = "Length must be equal to 64 symbols"
    ) String passwordHash,

    @NotBlank(message = "Field cannot be blank") @Length(
        min = 64, message = "Length must be greater or equal to 64 symbols"
    ) String publicKey,

    @NotBlank(message = "Field cannot be blank") @Length(
        min = 64, message = "Length must be greater or equal to 64 symbols"
    ) String encryptedPrivateKey
) {
}
