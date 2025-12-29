package minchakov.arkadii.amina.service;

import minchakov.arkadii.amina.dto.GetMessageDTO;
import minchakov.arkadii.amina.dto.UpdateMessageDTO;
import minchakov.arkadii.amina.model.Message;
import minchakov.arkadii.amina.model.User;
import minchakov.arkadii.amina.model.UserChatId;
import minchakov.arkadii.amina.repository.ChatRepository;
import minchakov.arkadii.amina.repository.MessageRepository;
import minchakov.arkadii.amina.repository.UserChatRepository;
import minchakov.arkadii.amina.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class MessageService extends CrudServiceImpl<Message, Integer> {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final UserChatRepository userChatRepository;

    public MessageService(
        MessageRepository messageRepository,
        ChatRepository chatRepository,
        UserRepository userRepository,
        UserChatRepository userChatRepository
    ) {
        super(messageRepository);
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.userChatRepository = userChatRepository;
    }

    public List<GetMessageDTO> listMessages(int chatId, User user) {
        user = userRepository.findById(user.getId()).orElse(null);
        if (user == null) {
            throw new RuntimeException("Cannot find user to list messages");
        }

        var chat = chatRepository.findById(chatId).orElse(null);
        if (chat == null || !userChatRepository.existsById(new UserChatId(user, chat))) {
            throw new RuntimeException("User doesn't have access to this chat");
        }

        var messages = messageRepository.getMessagesByChat_IdOrderByCreatedAtAsc(chatId);
        return messages.stream()
                       .map(m -> new GetMessageDTO(m.getSender().getUsername(), m.getContent(), m.getCreatedAt()))
                       .toList();
    }

    public LocalDateTime updateMessage(UpdateMessageDTO updateMessageDTO, int chatId, int id, User user) {
        user = userRepository.findById(user.getId()).orElse(null);
        if (user == null) {
            throw new RuntimeException("Cannot find user to update message");
        }

        var message = messageRepository.findById(id).orElse(null);
        if (message == null || !message.getSender().equals(user)) {
            throw new RuntimeException("User doesn't have access to this message");
        }

        message.setContent(updateMessageDTO.getContent());
        message = messageRepository.save(message);

        return message.getUpdatedAt();
    }
}
