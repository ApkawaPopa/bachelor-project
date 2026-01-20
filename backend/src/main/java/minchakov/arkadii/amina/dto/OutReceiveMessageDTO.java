package minchakov.arkadii.amina.dto;

import java.time.LocalDateTime;

public record OutReceiveMessageDTO(
    int id, String username, LocalDateTime receivedAt
) {
}
