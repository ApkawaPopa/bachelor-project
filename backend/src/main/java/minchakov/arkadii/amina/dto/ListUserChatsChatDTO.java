package minchakov.arkadii.amina.dto;

public class ListUserChatsChatDTO {

    private int id;
    private String name;
    private ListUserChatsMessageDTO message;

    public ListUserChatsChatDTO() {
    }

    public ListUserChatsChatDTO(int id, String name, ListUserChatsMessageDTO message) {
        this.id = id;
        this.name = name;
        this.message = message;
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

    public ListUserChatsMessageDTO getMessage() {
        return message;
    }

    public void setMessage(ListUserChatsMessageDTO message) {
        this.message = message;
    }
}
