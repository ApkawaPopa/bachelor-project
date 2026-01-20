package minchakov.arkadii.amina.service;

import minchakov.arkadii.amina.dto.GetMessageDTO;
import minchakov.arkadii.amina.dto.GetMessageReceiverDTO;
import minchakov.arkadii.amina.dto.UpdateMessageDTO;
import minchakov.arkadii.amina.exception.InternalServerErrorException;
import minchakov.arkadii.amina.model.Message;
import minchakov.arkadii.amina.model.User;
import minchakov.arkadii.amina.model.UserChatId;
import minchakov.arkadii.amina.repository.ChatRepository;
import minchakov.arkadii.amina.repository.MessageRepository;
import minchakov.arkadii.amina.repository.UserChatRepository;
import minchakov.arkadii.amina.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
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
            throw new InternalServerErrorException("Authenticated user not found in repository");
        }

        var chat = chatRepository.findById(chatId).orElse(null);
        if (chat == null || !userChatRepository.existsById(new UserChatId(user, chat))) {
            throw new AccessDeniedException("User doesn't have access to this chat");
        }

        var messages = messageRepository.getMessagesByChat_IdOrderByCreatedAtAsc(chatId);
        return messages.stream().map(m -> new GetMessageDTO(
            m.getId(),
            m.getSender().getUsername(),
            m.getContent(),
            m.getCreatedAt(),
            m.getReceivers()
             .stream()
             .map(r -> new GetMessageReceiverDTO(r.getReceiver().getUsername(), r.getCreatedAt()))
             .toList()
        )).toList();
    }

    public LocalDateTime updateMessage(UpdateMessageDTO updateMessageDTO, int id, User user) {
        user = userRepository.findById(user.getId()).orElse(null);
        if (user == null) {
            throw new InternalServerErrorException("Authenticated user not found in repository");
        }

        var message = messageRepository.findById(id).orElse(null);
        if (message == null || !message.getSender().equals(user)) {
            throw new AccessDeniedException("User doesn't have access to this message");
        }

        message.setContent(updateMessageDTO.content());
        message = messageRepository.save(message);

        return message.getUpdatedAt();
    }
}
