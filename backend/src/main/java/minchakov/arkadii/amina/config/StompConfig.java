package minchakov.arkadii.amina.config;

import minchakov.arkadii.amina.controller.advice.GlobalControllerAdvice;
import minchakov.arkadii.amina.exception.AppException;
import minchakov.arkadii.amina.exception.BadRequestException;
import minchakov.arkadii.amina.exception.InternalServerErrorException;
import minchakov.arkadii.amina.exception.NotFoundException;
import minchakov.arkadii.amina.interceptor.WebSocketHandshakeInterceptor;
import minchakov.arkadii.amina.model.User;
import minchakov.arkadii.amina.repository.MessageRepository;
import minchakov.arkadii.amina.repository.UserChatRepository;
import minchakov.arkadii.amina.repository.UserRepository;
import minchakov.arkadii.amina.util.AppAuthenticationToken;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class StompConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketHandshakeInterceptor webSocketHandshakeInterceptor;
    private final UserRepository userRepository;
    private final UserChatRepository userChatRepository;
    private final GlobalControllerAdvice globalControllerAdvice;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MessageRepository messageRepository;

    public StompConfig(
        WebSocketHandshakeInterceptor webSocketHandshakeInterceptor,
        UserRepository userRepository,
        UserChatRepository userChatRepository,
        GlobalControllerAdvice globalControllerAdvice,
        @Lazy SimpMessagingTemplate simpMessagingTemplate,
        MessageRepository messageRepository
    ) {
        this.webSocketHandshakeInterceptor = webSocketHandshakeInterceptor;
        this.userRepository = userRepository;
        this.userChatRepository = userChatRepository;
        this.globalControllerAdvice = globalControllerAdvice;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.messageRepository = messageRepository;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(webSocketHandshakeInterceptor)
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected @NonNull Principal determineUser(
                        ServerHttpRequest request,
                        WebSocketHandler wsHandler,
                        Map<String, Object> attributes
                    ) {
                        return new AppAuthenticationToken((User) attributes.get("user"));
                    }
                })
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                System.out.println("preSend processing начато");
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null) {
                    throw new InternalServerErrorException(
                        "Cannot access message headers and websocket session while authorizing message");
                }

                var principal = accessor.getUser();
                if (principal == null) {
                    throw new InternalServerErrorException("Principal is not set while authorizing message");
                }

                var username = principal.getName();

                try {
                    var user = userRepository.findByUsername(username)
                                             .orElseThrow(() -> new InternalServerErrorException(
                                                 "User not found while authorizing message"));
                    System.out.println("user: " + user.getUsername());

                    var command = accessor.getCommand();
                    if (command == null) {
                        throw new BadRequestException("Stomp command not set");
                    }
                    System.out.println(command);

                    switch (command) {
                        case SUBSCRIBE -> {
                            var destination = accessor.getDestination();
                            if (destination == null) {
                                throw NotFoundException.stompDefault();
                            }

                            System.out.println("Subscribe destination: " + destination);

                            var topicChat = "/topic/chat/";
                            if (destination.startsWith(topicChat)) {
                                var received = "/received";
                                if (destination.endsWith(received)) {
                                    destination = destination.substring(0, destination.length() - received.length());
                                }

                                var chatIdStr = destination.substring(topicChat.length());
                                try {
                                    var chatId = Integer.parseInt(chatIdStr);
                                    if (!userChatRepository.existsByUserAndChat_Id(user, chatId)) {
                                        throw new AccessDeniedException("You don't have access to this chat");
                                    }
                                } catch (NumberFormatException e) {
                                    throw NotFoundException.stompDefault();
                                }

                                return message;
                            }

                            if (!destination.equals("/user/queue/error") &&
                                !destination.equals("/user/queue/chat") &&
                                !destination.equals("/user/queue/unread")) {
                                throw NotFoundException.stompDefault();
                            }
                        }

                        case SEND -> {
                            var destination = accessor.getDestination();
                            if (destination == null) {
                                throw NotFoundException.stompDefault();
                            }

                            System.out.println("Message destination: " + destination);

                            var appChat = "/app/chat/";
                            if (!destination.startsWith(appChat)) {
                                throw NotFoundException.stompDefault();
                            }
                            destination = destination.substring(appChat.length());

                            var messagePost = "/message/post";
                            var receive = "/receive";
                            var edit = "/edit";
                            var delete = "/delete";
                            if (destination.endsWith(messagePost)) {
                                var chatIdStr = destination.substring(0, destination.length() - messagePost.length());

                                try {
                                    var chatId = Integer.parseInt(chatIdStr);
                                    if (!userChatRepository.existsByUserAndChat_Id(user, chatId)) {
                                        throw new AccessDeniedException("You don't have access to this chat");
                                    }
                                } catch (NumberFormatException e) {
                                    throw NotFoundException.stompDefault();
                                }
                            } else if (destination.endsWith(receive) || destination.endsWith(edit) ||
                                       destination.endsWith(delete)) {
                                var endIsReceive = destination.endsWith(receive);
                                destination = destination.substring(0, destination.lastIndexOf('/'));

                                var messageString = "/message/";
                                var messageStringIndex = destination.indexOf(messageString);
                                if (messageStringIndex == -1) {
                                    throw NotFoundException.stompDefault();
                                }

                                var chatIdStr = destination.substring(0, messageStringIndex);
                                var messageIdStr = destination.substring(messageStringIndex + messageString.length());
                                try {
                                    var chatId = Integer.parseInt(chatIdStr);
                                    if (!userChatRepository.existsByUserAndChat_Id(user, chatId)) {
                                        throw new AccessDeniedException("You don't have access to this chat");
                                    }

                                    var messageId = Integer.parseInt(messageIdStr);
                                    var chatMessage = messageRepository.findById(messageId)
                                                                       .orElseThrow(NotFoundException::stompDefault);
                                    if (chatMessage.getChat().getId() != chatId) {
                                        throw new NotFoundException("Message not found in selected chat");
                                    }

                                    if (!endIsReceive && !chatMessage.getSender().equals(user)) {
                                        throw new AccessDeniedException("You don't have access to this message");
                                    }
                                } catch (NumberFormatException e) {
                                    throw NotFoundException.stompDefault();
                                }
                            } else {
                                throw NotFoundException.stompDefault();
                            }
                        }
                    }

                    return message;
                } catch (Exception e) {
                    System.out.println("Found exception: " + e.getMessage());
                    simpMessagingTemplate.convertAndSendToUser(
                        username, "/queue/error", switch (e) {
                            case AppException appExc -> globalControllerAdvice.handleMessageAppException(appExc);
                            case AccessDeniedException accDenExc ->
                                globalControllerAdvice.handleMessageAccessDeniedException(accDenExc);
                            default -> globalControllerAdvice.handleMessageUnexpectedException(e);
                        }
                    );

                    return null;
                }
            }
        });
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }
}
