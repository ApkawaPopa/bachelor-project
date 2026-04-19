package minchakov.arkadii.amina.dto;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

public record ListUserChatsChatDTO(
    int id,
    String name, String encryptedSymmetricKey,
    String messageContent,
    LocalDateTime messageCreatedAt,
    LocalDateTime sortingDate,
    int unreadMessagesCount,
    int userCount,
    LocalDateTime invitedAt,
    List<URL> pictureUrls
) {
}
