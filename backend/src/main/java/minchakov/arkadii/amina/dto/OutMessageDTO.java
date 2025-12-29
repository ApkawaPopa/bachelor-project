package minchakov.arkadii.amina.dto;

import java.time.LocalDateTime;

public class OutMessageDTO {
    private String sender;

    private String content;

    private LocalDateTime createdAt;

    public OutMessageDTO() {
    }

    public OutMessageDTO(String sender, String content) {
        this.sender = sender;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
