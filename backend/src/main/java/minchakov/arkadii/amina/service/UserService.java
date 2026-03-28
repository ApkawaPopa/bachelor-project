package minchakov.arkadii.amina.service;

import minchakov.arkadii.amina.dto.GetUsersKeysOutDTO;
import minchakov.arkadii.amina.dto.GetUsersKeysUsernameDTO;
import minchakov.arkadii.amina.dto.ListUserChatsChatDTO;
import minchakov.arkadii.amina.exception.InternalServerErrorException;
import minchakov.arkadii.amina.model.Chat;
import minchakov.arkadii.amina.model.User;
import minchakov.arkadii.amina.repository.MessageReceiverRepository;
import minchakov.arkadii.amina.repository.MessageRepository;
import minchakov.arkadii.amina.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class UserService extends CrudServiceImpl<User, Integer> {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final MessageReceiverRepository messageReceiverRepository;

    public UserService(
        UserRepository userRepository,
        MessageRepository messageRepository,
        MessageReceiverRepository messageReceiverRepository
    ) {
        super(userRepository);
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.messageReceiverRepository = messageReceiverRepository;
    }

    @Transactional(readOnly = true)
    public List<ListUserChatsChatDTO> listUserChats(Integer id) {
        var user = userRepository.findById(id).orElse(null);

        if (user == null) {
            throw new RuntimeException("User not found by id: " + id);
        }

        var userChats = user.getUserChats();

        return userChats.stream().map(userChat -> {
            var chat = userChat.getChat();
            var userCount = chat.getChatUsers().size();

            var lastMessage = messageRepository.findFirstByChatOrderByCreatedAtDesc(chat);

            String messageContent;
            LocalDateTime messageCreatedAt;
            LocalDateTime sortingDate;

            if (lastMessage.isEmpty()) {
                messageContent = "";
                messageCreatedAt = null;
                sortingDate = userChat.getCreatedAt();
            } else {
                messageContent = lastMessage.get().getContent();
                messageCreatedAt = sortingDate = lastMessage.get().getCreatedAt();
            }

            return new ListUserChatsChatDTO(
                chat.getId(),
                chat.getName(),
                userChat.getEncryptedSymmetricKey(),
                messageContent,
                messageCreatedAt,
                sortingDate,
                getUnreadMessagesCount(chat, user),
                userCount
            );
        }).sorted(Comparator.comparing(ListUserChatsChatDTO::sortingDate).reversed()).toList();
    }

    public List<GetUsersKeysOutDTO> getUsersKeys(List<GetUsersKeysUsernameDTO> usernameDtos) {
        return usernameDtos.stream().map(dto -> {
            var user = userRepository.findByUsername(dto.username())
                                     .orElseThrow(() -> new InternalServerErrorException("Validator didn't work"));
            return new GetUsersKeysOutDTO(user.getUsername(), user.getPublicKey());
        }).toList();
    }

    public int getUnreadMessagesCount(Chat chat, User user) {
        return (int) (
            messageRepository.countMessageByChat(chat) -
            messageReceiverRepository.countByChatAndReceiver(chat, user)
        );
    }
}
