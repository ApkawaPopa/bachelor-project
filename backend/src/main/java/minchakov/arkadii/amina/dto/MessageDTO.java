package minchakov.arkadii.amina.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MessageDTO(
    @NotBlank(message = "Field cannot be blank") String content,

    @NotNull(message = "Field cannot be blank") Integer chatId
) {
}
