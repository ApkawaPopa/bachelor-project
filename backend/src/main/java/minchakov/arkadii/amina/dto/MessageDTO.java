package minchakov.arkadii.amina.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// DTO для входящих сообщений
public class MessageDTO {
    @NotBlank(message = "Field cannot be blank")
    private String content;

    @NotNull(message = "Field cannot be blank")
    private Integer chatId;

    public MessageDTO() {
    }

    public MessageDTO(String content, Integer chatId) {
        this.content = content;
        this.chatId = chatId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getChatId() {
        return chatId;
    }

    public void setChatId(Integer chatId) {
        this.chatId = chatId;
    }
}
