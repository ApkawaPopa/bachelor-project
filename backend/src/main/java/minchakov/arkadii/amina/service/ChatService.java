package minchakov.arkadii.amina.service;

import minchakov.arkadii.amina.dto.ChatCreateDTO;
import minchakov.arkadii.amina.dto.ChatCreationEvent;
import minchakov.arkadii.amina.dto.ChatDeletionEvent;
import minchakov.arkadii.amina.exception.InternalServerErrorException;
import minchakov.arkadii.amina.model.Chat;
import minchakov.arkadii.amina.model.User;
import minchakov.arkadii.amina.model.UserChat;
import minchakov.arkadii.amina.repository.ChatRepository;
import minchakov.arkadii.amina.repository.S3ObjectRepository;
import minchakov.arkadii.amina.repository.UserChatRepository;
import minchakov.arkadii.amina.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class ChatService {

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final UserChatRepository userChatRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final S3ObjectRepository s3ObjectRepository;
    private final S3Service s3Service;

    public ChatService(
        UserRepository userRepository,
        ChatRepository chatRepository,
        UserChatRepository userChatRepository,
        ApplicationEventPublisher applicationEventPublisher,
        S3Service s3Service,
        S3ObjectRepository s3ObjectRepository
    ) {
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.userChatRepository = userChatRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.s3Service = s3Service;
        this.s3ObjectRepository = s3ObjectRepository;
    }

    public Chat createChat(ChatCreateDTO chatCreateDTO) {
        var chat = saveChatWithUsers(chatCreateDTO);
        applicationEventPublisher.publishEvent(new ChatCreationEvent(chat.getId()));
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

    public Integer deleteChat(int chatId, User currentUser) {
        currentUser = userRepository.findById(currentUser.getId()).orElse(null);
        if (currentUser == null) {
            throw new InternalServerErrorException("Current logged-in user not found in repository");
        }

        var requestedChatOpt = chatRepository.findById(chatId);
        if (requestedChatOpt.isPresent()) {
            var requestedChat = requestedChatOpt.get();
            if (userChatRepository.existsByUserAndChat(currentUser, requestedChat)) {
                applicationEventPublisher.publishEvent(new ChatDeletionEvent(
                    chatId,
                    requestedChat
                        .getChatUsers()
                        .stream()
                        .map(UserChat::getUser)
                        .map(User::getId)
                        .toList()
                ));
                chatRepository.delete(requestedChat);
                return chatId;
            }
        }

        throw new AccessDeniedException("You don't have access to this chat");
    }

    public URL setChatPicture(int chatId, int objectId, User currentUser) {
        currentUser = userRepository
            .findByUsername(currentUser.getUsername())
            .orElseThrow(() -> new InternalServerErrorException("Current logged-in user not found in repository"));

        var chat = chatRepository.findById(chatId).orElse(null);
        if (chat == null || !userChatRepository.existsByUserAndChat(currentUser, chat)) {
            throw new AccessDeniedException("You don't have access to this chat");
        }

        var picture = s3ObjectRepository.findById(objectId).orElse(null);
        if (picture == null || !picture.getOwner().equals(currentUser)) {
            throw new AccessDeniedException("You don't have access to this picture");
        }
        if (picture.getConfirmedAt() != null) {
            throw new AccessDeniedException("Picture object already in use");
        }

        picture.setChat(chat);
        picture.setConfirmedAt(LocalDateTime.now());
        s3ObjectRepository.save(picture);

        return s3Service.getSignedGetUrls(List.of(objectId), currentUser).getFirst();
    }
}
