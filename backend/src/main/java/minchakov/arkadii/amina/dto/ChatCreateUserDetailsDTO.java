package minchakov.arkadii.amina.dto;

import jakarta.validation.constraints.NotBlank;
import minchakov.arkadii.amina.validator.ChatCreateUsernameConstraint;

import java.util.Objects;

public record ChatCreateUserDetailsDTO(
    @ChatCreateUsernameConstraint String username,
    @NotBlank(message = "Field cannot be empty") String encryptedSymmetricKey
) {

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChatCreateUserDetailsDTO that))
            return false;
        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(username);
    }
}
