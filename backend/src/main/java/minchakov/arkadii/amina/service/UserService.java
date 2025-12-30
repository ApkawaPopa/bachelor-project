package minchakov.arkadii.amina.service;

import minchakov.arkadii.amina.dto.ListUserChatsChatDTO;
import minchakov.arkadii.amina.model.User;
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

    public UserService(UserRepository userRepository, MessageRepository messageRepository) {
        super(userRepository);
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional(readOnly = true)
    public List<ListUserChatsChatDTO> listUserChats(Integer id) {
        var user = userRepository.findById(id).orElse(null);

        if (user == null) {
            throw new RuntimeException("User not found by id: " + id);
        }

        var chats = user.getChats();

        return chats.stream().map(chat -> {
            var lastMessage = messageRepository.findFirstByChatOrderByCreatedAtDesc(chat);

            String messageContent;
            LocalDateTime messageCreatedAt;
            LocalDateTime sortingDate;

            if (lastMessage.isEmpty()) {
                messageContent = "";
                messageCreatedAt = null;
                sortingDate = chat.getCreatedAt();
            } else {
                messageContent = lastMessage.get().getContent();
                messageCreatedAt = sortingDate = lastMessage.get().getCreatedAt();
            }

            return new ListUserChatsChatDTO(
                chat.getId(),
                chat.getName(),
                messageContent,
                messageCreatedAt,
                sortingDate
            );
        }).sorted(Comparator.comparing(ListUserChatsChatDTO::sortingDate).reversed()).toList();
    }
}
