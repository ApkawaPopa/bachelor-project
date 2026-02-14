package minchakov.arkadii.amina.dto;

import java.time.LocalDateTime;

public record OutEditMessageDTO(
    int id, String content, LocalDateTime updatedAt
) {
}
