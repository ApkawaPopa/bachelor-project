package minchakov.arkadii.amina.controller;

import jakarta.validation.Valid;
import minchakov.arkadii.amina.dto.InMessageDTO;
import minchakov.arkadii.amina.dto.OutMessageDTO;
import minchakov.arkadii.amina.model.Message;
import minchakov.arkadii.amina.model.User;
import minchakov.arkadii.amina.model.UserChatId;
import minchakov.arkadii.amina.repository.ChatRepository;
import minchakov.arkadii.amina.repository.MessageRepository;
import minchakov.arkadii.amina.repository.UserChatRepository;
import minchakov.arkadii.amina.repository.UserRepository;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Controller
@Transactional
public class StompMessageController {

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserChatRepository userChatRepository;

    public StompMessageController(
        UserRepository userRepository,
        ChatRepository chatRepository,
        MessageRepository messageRepository,
        UserChatRepository userChatRepository
    ) {
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.userChatRepository = userChatRepository;
    }

    @MessageMapping("/chat/{id}/message/post")
    @SendTo("/topic/chat/{id}")
    public OutMessageDTO sendMessage(
        @Valid InMessageDTO inMessageDTO,
        @DestinationVariable("id") int id, SimpMessageHeaderAccessor headerAccessor
    ) {
        Map<String, Object> attrs = headerAccessor.getSessionAttributes();
        if (attrs == null) {
            throw new RuntimeException("WebSocket session attributes was null while sending message");
        }

        var chat = chatRepository.findById(id).orElse(null);
        if (chat != null) {
            if (attrs.get("user") instanceof User principal) {
                var user = userRepository.findById(principal.getId()).orElse(null);
                if (user != null) {
                    if (userChatRepository.existsById(new UserChatId(user, chat))) {
                        var savedMessage = messageRepository.save(new Message(user, chat, inMessageDTO.content()));
                        return new OutMessageDTO(
                            user.getUsername(),
                            inMessageDTO.content(),
                            savedMessage.getCreatedAt()
                        );
                    }
                }
            } else {
                throw new RuntimeException("User not found in WebSocket session attributes while sending message");
            }
        }

        throw new AccessDeniedException("You don't have access to this chat");
    }

    @MessageMapping("/chat")
    public Map<String, Object> addChat(Integer chatId, SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> attrs = headerAccessor.getSessionAttributes();
        if (attrs == null) {
            throw new RuntimeException("WebSocket session attributes was null while adding chat");
        }

        var principal = (User) attrs.get("user");
        var user = userRepository.findById(principal.getId()).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found in WebSocket session attributes while adding chat");
        }

        var chat = chatRepository.findById(chatId).orElse(null);
        if (chat == null || !user.getChats().contains(chat)) {
            throw new AccessDeniedException("You don't have access to this chat");
        }

        return Map.of("id", chatId, "name", chat.getName());
    }
}
