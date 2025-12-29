package minchakov.arkadii.amina.model;

import java.io.Serializable;
import java.util.Objects;

public class UserChatId implements Serializable {

    private User user;

    private Chat chat;

    public UserChatId() {
    }

    public UserChatId(User user, Chat chat) {
        this.user = user;
        this.chat = chat;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserChatId that))
            return false;
        return Objects.equals(user, that.user) && Objects.equals(chat, that.chat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, chat);
    }
}