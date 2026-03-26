package minchakov.arkadii.amina.service;

import minchakov.arkadii.amina.dto.GetMessageDTO;
import minchakov.arkadii.amina.dto.GetMessageFileDTO;
import minchakov.arkadii.amina.dto.GetMessageReceiverDTO;
import minchakov.arkadii.amina.exception.InternalServerErrorException;
import minchakov.arkadii.amina.model.Message;
import minchakov.arkadii.amina.model.User;
import minchakov.arkadii.amina.model.UserChatId;
import minchakov.arkadii.amina.repository.MessageRepository;
import minchakov.arkadii.amina.repository.UserChatRepository;
import minchakov.arkadii.amina.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MessageService extends CrudServiceImpl<Message, Integer> {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final UserChatRepository userChatRepository;

    public MessageService(
        MessageRepository messageRepository,
        UserRepository userRepository,
        UserChatRepository userChatRepository
    ) {
        super(messageRepository);
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.userChatRepository = userChatRepository;
    }

    public List<GetMessageDTO> listMessages(int chatId, User user) {
        user = userRepository.findById(user.getId()).orElse(null);
        if (user == null) {
            throw new InternalServerErrorException("Authenticated user not found in repository");
        }

        if (!userChatRepository.existsById(new UserChatId(user.getId(), chatId))) {
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
             .toList(),
            m.getS3Objects()
             .stream()
             .map(obj -> new GetMessageFileDTO(obj.getId(), obj.getEncryptedName()))
             .toList()
        )).toList();
    }
}
