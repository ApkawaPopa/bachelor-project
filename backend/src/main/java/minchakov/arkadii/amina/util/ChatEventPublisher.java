package minchakov.arkadii.amina.util;

import minchakov.arkadii.amina.dto.AddChatDTO;
import minchakov.arkadii.amina.dto.ChatCreationEvent;
import minchakov.arkadii.amina.dto.StompResponse;
import minchakov.arkadii.amina.exception.InternalServerErrorException;
import minchakov.arkadii.amina.repository.ChatRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ChatEventPublisher {

    private final ChatRepository chatRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public ChatEventPublisher(ChatRepository chatRepository, SimpMessagingTemplate simpMessagingTemplate) {
        this.chatRepository = chatRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendChatCreationEvent(ChatCreationEvent event) {
        var chat = chatRepository.findById(event.chatId())
                                 .orElseThrow(() -> new InternalServerErrorException(
                                     "Chat not found by id after creation"));
        for (var chatUser : chat.getChatUsers()) {
            var user = chatUser.getUser();
            var stompResponse = StompResponse.success(new AddChatDTO(
                chat.getId(),
                chat.getName(),
                chatUser.getEncryptedSymmetricKey()
            ));
            System.out.println(user.getUsername());
            simpMessagingTemplate.convertAndSendToUser(user.getUsername(), "/queue/chat", stompResponse);
        }
    }

}
