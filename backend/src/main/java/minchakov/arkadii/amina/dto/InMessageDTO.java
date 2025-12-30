package minchakov.arkadii.amina.dto;

import jakarta.validation.constraints.NotBlank;

public record InMessageDTO(
    @NotBlank(message = "Field cannot be blank") String content
) {
}
