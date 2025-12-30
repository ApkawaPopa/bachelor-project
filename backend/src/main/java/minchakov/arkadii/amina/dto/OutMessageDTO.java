package minchakov.arkadii.amina.dto;

import java.time.LocalDateTime;

public record OutMessageDTO(
    String sender, String content, LocalDateTime createdAt
) {
}
