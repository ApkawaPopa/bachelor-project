package minchakov.arkadii.amina.dto;

import java.util.List;

public record ReadChatDTO(
    String name, List<ReadChatMessageDTO> messages
) {
}
