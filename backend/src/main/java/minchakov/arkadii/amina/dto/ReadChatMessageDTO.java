package minchakov.arkadii.amina.dto;

import java.time.LocalDateTime;

public record ReadChatMessageDTO(
    String content, String sender, LocalDateTime createdAt
) {
}
