package minchakov.arkadii.amina.dto;

public class RegisterDTO {

    private String username;

    private String passwordHash;

    private String publicKey;

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
