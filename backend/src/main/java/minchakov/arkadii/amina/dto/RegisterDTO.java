package minchakov.arkadii.amina.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public class RegisterDTO {

    @NotBlank(message = "Field cannot be blank")
    private String username;

    @NotBlank(message = "Field cannot be blank")
    @Length(min = 64, max = 64, message = "Length must be 64 symbols")
    private String passwordHash;

    @NotBlank(message = "Field cannot be blank")
    @Length(min = 64, message = "Length must be greater or equal to 64 symbols")
    private String publicKey;

    @NotBlank(message = "Field cannot be blank")
    @Length(min = 64, message = "Length must be greater or equal to 64 symbols")
    private String encryptedPrivateKey;

    public RegisterDTO() {
    }

    public RegisterDTO(String username, String passwordHash, String publicKey, String encryptedPrivateKey) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.publicKey = publicKey;
        this.encryptedPrivateKey = encryptedPrivateKey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    public void setEncryptedPrivateKey(String encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
    }
}
