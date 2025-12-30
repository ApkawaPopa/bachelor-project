package minchakov.arkadii.amina.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateMessageDTO(@NotBlank(message = "This field cannot be blank") String content) {
}
