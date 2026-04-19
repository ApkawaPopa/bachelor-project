package minchakov.arkadii.amina.util;

import minchakov.arkadii.amina.dto.AddChatDTO;
import minchakov.arkadii.amina.dto.ChatCreationEvent;
import minchakov.arkadii.amina.dto.ChatDeletionEvent;
import minchakov.arkadii.amina.dto.ChatMessageEvent;
import minchakov.arkadii.amina.dto.StompResponse;
import minchakov.arkadii.amina.dto.UnreadMessagesCountDTO;
import minchakov.arkadii.amina.exception.InternalServerErrorException;
import minchakov.arkadii.amina.repository.ChatRepository;
import minchakov.arkadii.amina.repository.UserRepository;
import minchakov.arkadii.amina.service.S3Service;
import minchakov.arkadii.amina.service.UserService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ChatEventPublisher {

    private final ChatRepository chatRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserService userService;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    public ChatEventPublisher(
        ChatRepository chatRepository,
        SimpMessagingTemplate simpMessagingTemplate,
        UserService userService,
        UserRepository userRepository,
        S3Service s3Service
    ) {
        this.chatRepository = chatRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.userService = userService;
        this.userRepository = userRepository;
        this.s3Service = s3Service;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendChatCreationEvent(ChatCreationEvent event) {
        var chat = chatRepository.findById(event.chatId())
                                 .orElseThrow(() -> new InternalServerErrorException(
                                     "Chat not found by id after creation"));
        var chatUsers = chat.getChatUsers();
        var userCount = chatUsers.size();
        for (var chatUser : chatUsers) {
            var user = chatUser.getUser();
            var stompResponse = StompResponse.success(new AddChatDTO(
                chat.getId(),
                chat.getName(),
                chatUser.getEncryptedSymmetricKey(),
                chat.getCreatedAt(),
                userCount,
                s3Service.getChatPictures(chat)
            ));
            System.out.println(user.getUsername());
            simpMessagingTemplate.convertAndSendToUser(user.getUsername(), "/queue/chat/created", stompResponse);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendChatDeletionEvent(ChatDeletionEvent event) {
        var chatUsers = event
            .userIds()
            .stream()
            .map(id -> userRepository
                .findById(id)
                .orElseThrow(() -> new InternalServerErrorException("User with id" + id + " not found after deletion")))
            .toList();
        for (var user : chatUsers) {
            var stompResponse = StompResponse.success(event.chatId());
            System.out.println(user.getUsername());
            simpMessagingTemplate.convertAndSendToUser(user.getUsername(), "/queue/chat/deleted", stompResponse);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendChatMessageEvent(ChatMessageEvent event) {
        var chat = chatRepository.findById(event.chatId())
                                 .orElseThrow(() -> new InternalServerErrorException(
                                     "Chat not found by id on chat message event"));
        if (event.username() == null) {
            for (var chatUser : chat.getChatUsers()) {
                var user = chatUser.getUser();
                var stompResponse = StompResponse.success(new UnreadMessagesCountDTO(
                    event.chatId(),
                    userService.getUnreadMessagesCount(
                        chat,
                        user
                    )
                ));
                simpMessagingTemplate.convertAndSendToUser(user.getUsername(), "/queue/unread", stompResponse);
            }
        } else {
            var user = userRepository.findByUsername(event.username())
                                     .orElseThrow(() -> new InternalServerErrorException(
                                         "User not found by username on chat message event"));
            var stompResponse = StompResponse.success(new UnreadMessagesCountDTO(
                event.chatId(),
                userService.getUnreadMessagesCount(
                    chat,
                    user
                )
            ));
            simpMessagingTemplate.convertAndSendToUser(user.getUsername(), "/queue/unread", stompResponse);
        }
    }

}
