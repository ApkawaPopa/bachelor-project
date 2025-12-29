package minchakov.arkadii.amina.dto;

import java.time.LocalDateTime;

public class ListUserChatsChatDTO {

    private int id;
    private String name;
    private String messageContent;
    private LocalDateTime messageCreatedAt;

    public ListUserChatsChatDTO() {
    }

    public ListUserChatsChatDTO(int id, String name, String messageContent, LocalDateTime messageCreatedAt) {
        this.id = id;
        this.name = name;
        this.messageContent = messageContent;
        this.messageCreatedAt = messageCreatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public LocalDateTime getMessageCreatedAt() {
        return messageCreatedAt;
    }

    public void setMessageCreatedAt(LocalDateTime messageCreatedAt) {
        this.messageCreatedAt = messageCreatedAt;
    }
}
