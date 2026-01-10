package minchakov.arkadii.amina.controller;

import jakarta.validation.Valid;
import minchakov.arkadii.amina.dto.InMessageDTO;
import minchakov.arkadii.amina.dto.OutMessageDTO;
import minchakov.arkadii.amina.dto.StompResponse;
import minchakov.arkadii.amina.exception.InternalServerErrorException;
import minchakov.arkadii.amina.model.Message;
import minchakov.arkadii.amina.repository.ChatRepository;
import minchakov.arkadii.amina.repository.MessageRepository;
import minchakov.arkadii.amina.repository.UserChatRepository;
import minchakov.arkadii.amina.repository.UserRepository;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

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
    public StompResponse<OutMessageDTO> sendMessage(
        @Valid InMessageDTO inMessageDTO,
        @DestinationVariable("id") int id,
        Principal principal
    ) {
        var chat = chatRepository.findById(id).orElse(null);
        if (chat != null) {
            var user = userRepository.findByUsername(principal.getName())
                                     .orElseThrow(() -> new InternalServerErrorException(
                                         "User not found from principal while processing message"));
            if (userChatRepository.existsByUserAndChat(user, chat)) {
                var savedMessage = messageRepository.save(new Message(user, chat, inMessageDTO.content()));
                return StompResponse.success(new OutMessageDTO(
                    user.getUsername(),
                    inMessageDTO.content(),
                    savedMessage.getCreatedAt()
                ));
            }
        }
        throw new AccessDeniedException("You don't have access to this chat");
    }
}
