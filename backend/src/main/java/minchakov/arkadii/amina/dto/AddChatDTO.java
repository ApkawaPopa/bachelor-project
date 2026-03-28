package minchakov.arkadii.amina.dto;

import java.time.LocalDateTime;

public record AddChatDTO(
    Integer id, String name, String encryptedSymmetricKey, LocalDateTime createdAt, Integer userCount
) {
}
