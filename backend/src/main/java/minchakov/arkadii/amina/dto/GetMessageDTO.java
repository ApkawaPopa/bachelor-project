package minchakov.arkadii.amina.dto;

import java.time.LocalDateTime;

public record GetMessageDTO(
    String sender, String content, LocalDateTime createdAt
) {
}
