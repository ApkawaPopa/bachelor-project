package minchakov.arkadii.amina.controller;

import jakarta.validation.Valid;
import minchakov.arkadii.amina.dto.InSendMessageDTO;
import minchakov.arkadii.amina.dto.OutReceiveMessageDTO;
import minchakov.arkadii.amina.dto.OutSendMessageDTO;
import minchakov.arkadii.amina.dto.StompResponse;
import minchakov.arkadii.amina.exception.BadRequestException;
import minchakov.arkadii.amina.exception.InternalServerErrorException;
import minchakov.arkadii.amina.model.Message;
import minchakov.arkadii.amina.model.MessageReceiver;
import minchakov.arkadii.amina.model.MessageReceiverId;
import minchakov.arkadii.amina.repository.ChatRepository;
import minchakov.arkadii.amina.repository.MessageReceiverRepository;
import minchakov.arkadii.amina.repository.MessageRepository;
import minchakov.arkadii.amina.repository.UserRepository;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Controller
@Transactional
public class StompMessageController {

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final MessageReceiverRepository messageReceiverRepository;

    public StompMessageController(
        UserRepository userRepository,
        ChatRepository chatRepository,
        MessageRepository messageRepository, MessageReceiverRepository messageReceiverRepository
    ) {
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.messageReceiverRepository = messageReceiverRepository;
    }

    @MessageMapping("/chat/{id}/message/post")
    @SendTo("/topic/chat/{id}")
    public StompResponse<OutSendMessageDTO> sendMessage(
        @Valid InSendMessageDTO inSendMessageDTO,
        @DestinationVariable("id") int id,
        Principal principal
    ) {
        var chat = chatRepository.findById(id)
                                 .orElseThrow(() -> new InternalServerErrorException(
                                     "Chat not found while processing message"));
        var user = userRepository.findByUsername(principal.getName())
                                 .orElseThrow(() -> new InternalServerErrorException(
                                     "User not found from principal while processing message"));
        var savedMessage = messageRepository.save(new Message(user, chat, inSendMessageDTO.content()));
        return StompResponse.success(new OutSendMessageDTO(
            savedMessage.getId(),
            user.getUsername(),
            inSendMessageDTO.content(),
            savedMessage.getCreatedAt()
        ));
    }

    @MessageMapping("/chat/{chatId}/message/{messageId}/receive")
    @SendTo("/topic/chat/{chatId}/received")
    public StompResponse<OutReceiveMessageDTO> receiveMessage(
        @DestinationVariable("chatId") int chatId,
        @DestinationVariable("messageId") int messageId,
        Principal principal
    ) {
        var message = messageRepository.findById(messageId)
                                       .orElseThrow(() -> new InternalServerErrorException(
                                           "Message not found while processing receive"));
        var receiver = userRepository.findByUsername(principal.getName())
                                     .orElseThrow(() -> new InternalServerErrorException(
                                         "User not found from principal while processing message"));

        if (messageReceiverRepository.existsById(new MessageReceiverId(message, receiver))) {
            throw new BadRequestException("Message already received by this user");
        }

        var messageReceiver = messageReceiverRepository.save(new MessageReceiver(message, receiver));
        return StompResponse.success(new OutReceiveMessageDTO(
            messageId,
            principal.getName(),
            messageReceiver.getCreatedAt()
        ));
    }
}
