package minchakov.arkadii.amina.dto;

public class LoginDTO {

    private String username;

    private String passwordHash;

    public LoginDTO() {
    }

    public LoginDTO(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
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
}
