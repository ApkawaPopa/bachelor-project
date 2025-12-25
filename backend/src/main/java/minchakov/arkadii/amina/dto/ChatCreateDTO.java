package minchakov.arkadii.amina.dto;

import java.util.List;

public class ChatCreateDTO {

    private List<String> usernames;

    public ChatCreateDTO() {
    }

    public ChatCreateDTO(List<String> usernames) {
        this.usernames = usernames;
    }

    public List<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(List<String> usernames) {
        this.usernames = usernames;
    }
}
