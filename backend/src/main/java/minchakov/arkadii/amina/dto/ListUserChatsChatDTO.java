package minchakov.arkadii.amina.dto;

import java.time.LocalDateTime;

public record ListUserChatsChatDTO(
    int id,
    String name, String encryptedSymmetricKey,
    String messageContent,
    LocalDateTime messageCreatedAt,
    LocalDateTime sortingDate,
    int unreadMessagesCount,
    int userCount,
    LocalDateTime invitedAt
) {
}
