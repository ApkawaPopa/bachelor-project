package minchakov.arkadii.amina.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import minchakov.arkadii.amina.validator.ChatCreateOwnUsernameConstraint;

import java.util.List;

public record ChatCreateDTO(
    @NotBlank(message = "Field cannot be blank") String chatName,

    @NotEmpty(message = "Field cannot be empty") @ChatCreateOwnUsernameConstraint List<@Valid ChatCreateUserDetailsDTO> usersDetails
) {
}
