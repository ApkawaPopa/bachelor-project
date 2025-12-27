package minchakov.arkadii.amina.controller;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Controller
public class MessageWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    private final Map<Integer, WebSocketSession> chatSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public MessageWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        sessions.add(session);

    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession sender, @NonNull TextMessage message) throws Exception {
        var messageJSON = objectMapper.readTree(message.getPayload());
        var messageContent = messageJSON.get("content").asString();
        var messageChatId = messageJSON.get("chatId").asString();

        for (var receiver : sessions) {
            receiver.sendMessage(message);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status) {
        sessions.remove(session);
    }
}
