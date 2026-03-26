package minchakov.arkadii.amina.model;

import java.io.Serializable;
import java.util.Objects;

public class UserChatId implements Serializable {

    private Integer user;

    private Integer chat;

    public UserChatId() {
    }

    public UserChatId(Integer user, Integer chat) {
        this.user = user;
        this.chat = chat;
    }

    public Integer getUser() {
        return user;
    }

    public void setUser(Integer user) {
        this.user = user;
    }

    public Integer getChat() {
        return chat;
    }

    public void setChat(Integer chat) {
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