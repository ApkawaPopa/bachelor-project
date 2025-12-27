package minchakov.arkadii.amina.dto;

import java.util.List;

public class ReadChatDTO {

    private String name;

    private List<ReadChatMessageDTO> messages;

    public ReadChatDTO() {
    }

    public ReadChatDTO(String name, List<ReadChatMessageDTO> messages) {
        this.name = name;
        this.messages = messages;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ReadChatMessageDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<ReadChatMessageDTO> messages) {
        this.messages = messages;
    }
}
