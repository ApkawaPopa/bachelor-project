package minchakov.arkadii.amina.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "message_receiver")
@IdClass(MessageReceiverId.class)
public class MessageReceiver {

    @Id
    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @Id
    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public MessageReceiver() {
    }

    public MessageReceiver(Message message, User receiver, Chat chat) {
        this.message = message;
        this.receiver = receiver;
        this.chat = chat;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
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

    @PrePersist
    private void setSavingTimestamp() {
        createdAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MessageReceiver that))
            return false;
        return Objects.equals(message, that.message) && Objects.equals(receiver, that.receiver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, receiver);
    }
}
