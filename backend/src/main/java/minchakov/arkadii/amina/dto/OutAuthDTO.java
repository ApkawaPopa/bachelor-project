package minchakov.arkadii.amina.dto;

public record OutAuthDTO(
    String jwt, String username, String publicKey, String encryptedPrivateKey
) {
}
