package minchakov.arkadii.amina.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "user_chat")
@IdClass(UserChatId.class)
public class UserChat {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public UserChat() {
    }

    public UserChat(User user, Chat chat) {
        this.user = user;
        this.chat = chat;
    }

    @PrePersist
    private void setSavingTimestamp() {
        createdAt = LocalDateTime.now();
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserChat userChat))
            return false;
        return Objects.equals(user, userChat.user) && Objects.equals(chat, userChat.chat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, chat);
    }
}
