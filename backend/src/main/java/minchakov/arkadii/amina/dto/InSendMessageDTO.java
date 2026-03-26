package minchakov.arkadii.amina.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public record InSendMessageDTO(
    String content,
    @Size(max = 15, message = "You cannot attach more than 15 files")
    List<Integer> fileKeys
) {
}
