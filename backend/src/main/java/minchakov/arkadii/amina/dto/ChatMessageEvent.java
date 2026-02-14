package minchakov.arkadii.amina.dto;

public record ChatMessageEvent(int chatId, String username) {
    public ChatMessageEvent(int chatId) {
        this(chatId, null);
    }
}
