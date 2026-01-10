package minchakov.arkadii.amina.dto;

public record AddChatDTO(
    Integer id, String name, String encryptedSymmetricKey
) {
}
