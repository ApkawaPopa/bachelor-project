package minchakov.arkadii.amina.dto;

import jakarta.validation.constraints.NotBlank;

public record InEditMessageDTO(
    @NotBlank(message = "Field cannot be blank") String content
) {
}
