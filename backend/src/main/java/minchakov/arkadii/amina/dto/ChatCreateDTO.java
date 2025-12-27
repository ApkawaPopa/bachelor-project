package minchakov.arkadii.amina.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public class ChatCreateDTO {

    @NotBlank(message = "Field cannot be blank")
    private String chatName;

    @NotEmpty(message = "Field cannot be empty")
    private Set<String> usernames;

    public ChatCreateDTO() {
    }

    public ChatCreateDTO(String chatName, Set<String> usernames) {
        this.chatName = chatName;
        this.usernames = usernames;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public Set<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(Set<String> usernames) {
        this.usernames = usernames;
    }
}
