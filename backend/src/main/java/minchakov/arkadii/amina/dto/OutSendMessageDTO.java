package minchakov.arkadii.amina.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OutSendMessageDTO(
    int id, String sender, String content, LocalDateTime createdAt, List<OutSendMessageFileDTO> fileKeys
) {
}
