package minchakov.arkadii.amina.service;

import minchakov.arkadii.amina.dto.AddChatDTO;
import minchakov.arkadii.amina.dto.ChatCreateDTO;
import minchakov.arkadii.amina.dto.StompResponse;
import minchakov.arkadii.amina.exception.InternalServerErrorException;
import minchakov.arkadii.amina.model.Chat;
import minchakov.arkadii.amina.model.User;
import minchakov.arkadii.amina.model.UserChat;
import minchakov.arkadii.amina.repository.ChatRepository;
import minchakov.arkadii.amina.repository.UserChatRepository;
import minchakov.arkadii.amina.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class ChatService {

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final UserChatRepository userChatRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public ChatService(
        UserRepository userRepository,
        ChatRepository chatRepository,
        UserChatRepository userChatRepository,
        SimpMessagingTemplate simpMessagingTemplate
    ) {
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.userChatRepository = userChatRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public Chat createChat(ChatCreateDTO chatCreateDTO) {
        var chat = saveChatWithUsers(chatCreateDTO);
        sendChatCreationEvent(chat); // TODO: вынести за транзакцию, а иначе она не успевает завершиться до подписки клиентов на связанные с чатом топики
        return chat;
    }

    private Chat saveChatWithUsers(ChatCreateDTO chatCreateDTO) {
        var chat = new Chat(chatCreateDTO.chatName());
        chat.setChatUsers(new HashSet<>());

        var usersDetails = new HashSet<>(chatCreateDTO.usersDetails());
        for (var userDetails : usersDetails) {
            var user = userRepository.findByUsername(userDetails.username())
                                     .orElseThrow(() -> new InternalServerErrorException(
                                         "Validator of chat dto didn't work"));
            chat.getChatUsers().add(new UserChat(user, chat, userDetails.encryptedSymmetricKey()));
        }

        chat = chatRepository.save(chat);
        userChatRepository.saveAll(chat.getChatUsers());

        return chat;
    }

    private void sendChatCreationEvent(Chat chat) {
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

    @Transactional(readOnly = true)
    public Chat getChat(int chatId, User currentUser) {
        var chatUser = getChatUser(chatId, currentUser);
        return chatUser.getChat();
    }

    @Transactional(readOnly = true)
    public UserChat getChatUser(int chatId, User currentUser) {
        currentUser = userRepository.findById(currentUser.getId()).orElse(null);
        if (currentUser == null) {
            throw new InternalServerErrorException("Current logged-in user not found in repository");
        }

        var userChat = userChatRepository.findByUser_IdAndChat_Id(currentUser.getId(), chatId);
        if (userChat.isPresent()) {
            return userChat.get();
        }

        throw new AccessDeniedException("You don't have access to this chat");
    }

    @Transactional(readOnly = true)
    public Set<UserChat> getChatUsers(int chatId, User currentUser) {
        currentUser = userRepository.findById(currentUser.getId()).orElse(null);
        if (currentUser == null) {
            throw new InternalServerErrorException("Current logged-in user not found in repository");
        }

        var requestedChatOpt = chatRepository.findById(chatId);
        if (requestedChatOpt.isPresent()) {
            var requestedChat = requestedChatOpt.get();
            if (userChatRepository.existsByUserAndChat(currentUser, requestedChat)) {
                return requestedChat.getChatUsers();
            }
        }

        throw new AccessDeniedException("You don't have access to this chat");
    }
}
