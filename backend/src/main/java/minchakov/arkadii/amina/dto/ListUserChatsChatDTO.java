package minchakov.arkadii.amina.dto;

import java.time.LocalDateTime;

public record ListUserChatsChatDTO(
    int id,
    String name,
    String encryptedPrivateKey,
    String messageContent,
    LocalDateTime messageCreatedAt,
    LocalDateTime sortingDate
) {
}
