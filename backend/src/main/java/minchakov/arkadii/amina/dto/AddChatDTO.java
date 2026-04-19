package minchakov.arkadii.amina.dto;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

public record AddChatDTO(
    Integer id,
    String name,
    String encryptedSymmetricKey,
    LocalDateTime invitedAt,
    Integer userCount,
    List<URL> pictureUrls
) {
}
