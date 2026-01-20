package minchakov.arkadii.amina.dto;

import java.time.LocalDateTime;

public record OutSendMessageDTO(
    int id, String sender, String content, LocalDateTime createdAt
) {
}
