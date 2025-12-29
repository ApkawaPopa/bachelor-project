package minchakov.arkadii.amina.dto;

import java.time.LocalDateTime;

public class GetMessageDTO {
    private String sender;

    private String content;

    private LocalDateTime createdAt;

    public GetMessageDTO() {
    }

    public GetMessageDTO(String sender, String content, LocalDateTime createdAt) {
        this.sender = sender;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
