package minchakov.arkadii.amina.dto;

import java.time.LocalDateTime;

public record ReadChatMessageDTO(
    int id, String content, String sender, LocalDateTime createdAt
) {
}
