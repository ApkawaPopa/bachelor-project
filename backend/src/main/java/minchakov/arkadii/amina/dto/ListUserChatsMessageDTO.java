package minchakov.arkadii.amina.dto;

public class ListUserChatsMessageDTO {

    private String senderName;

    private String content;

    public ListUserChatsMessageDTO() {
    }

    public ListUserChatsMessageDTO(String senderName, String content) {
        this.senderName = senderName;
        this.content = content;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
