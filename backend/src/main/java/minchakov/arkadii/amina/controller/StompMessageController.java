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
        //        BindingResult errors,
        @DestinationVariable("id") int id, SimpMessageHeaderAccessor headerAccessor
    ) {
        System.out.println("Обрабатываем сообщение: " + inMessageDTO.getContent());
        System.out.println("Обрабатываем сообщение: " + id);

        Map<String, Object> attrs = headerAccessor.getSessionAttributes();
        if (attrs == null) {
            throw new RuntimeException("Session attributes was null");
        }

        var principal = (User) attrs.get("user");

        var chat = chatRepository.findById(id).orElse(null);
        if (chat != null) {
            if (principal != null) {
                var user = userRepository.findByUsername(principal.getUsername()).orElse(null);
                if (user != null) {
                    if (userChatRepository.existsById(new UserChatId(user, chat))) {
                        messageRepository.save(new Message(user, chat, inMessageDTO.getContent()));
                        return new OutMessageDTO(user.getUsername(), inMessageDTO.getContent());
                    }
                }
            }
        }

        throw new AccessDeniedException("You don't have access to this chat");
    }

    @MessageMapping("/chat")
    public Map<String, Object> addChat(Integer chatId, SimpMessageHeaderAccessor headerAccessor) {
        Map<String, Object> attrs = headerAccessor.getSessionAttributes();
        if (attrs == null) {
            throw new RuntimeException("Session attributes was null");
        }

        var principal = (User) attrs.get("user");
        var user = userRepository.findById(principal.getId()).orElse(null);
        if (user == null) {
            throw new RuntimeException("User not found while adding chat in /app/chat");
        }

        var chat = chatRepository.findById(chatId).orElse(null);
        if (chat == null || !user.getChats().contains(chat)) {
            throw new AccessDeniedException("You don't have access to this chat");
        }

        return Map.of("id", chatId, "name", chat.getName());
    }
}
