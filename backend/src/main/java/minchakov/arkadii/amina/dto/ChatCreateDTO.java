package minchakov.arkadii.amina.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record ChatCreateDTO(
    @NotBlank(message = "Field cannot be blank") String chatName,

    @NotEmpty(message = "Field cannot be empty") Set<String> usernames
) {
}
