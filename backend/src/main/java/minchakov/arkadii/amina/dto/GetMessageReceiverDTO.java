package minchakov.arkadii.amina.dto;

import java.time.LocalDateTime;

public record GetMessageReceiverDTO(String username, LocalDateTime receivedAt) {
}
