package minchakov.arkadii.amina.controller;

import jakarta.validation.Valid;
import minchakov.arkadii.amina.dto.ChatCreateDTO;
import minchakov.arkadii.amina.dto.ListChatUsersUserDTO;
import minchakov.arkadii.amina.dto.ReadChatDTO;
import minchakov.arkadii.amina.dto.ReadChatMessageDTO;
import minchakov.arkadii.amina.dto.RestResponse;
import minchakov.arkadii.amina.model.Chat;
import minchakov.arkadii.amina.model.User;
import minchakov.arkadii.amina.repository.ChatRepository;
import minchakov.arkadii.amina.repository.UserRepository;
import minchakov.arkadii.amina.validator.ChatCreateDTOValidator;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;

@RestController
@Transactional
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatCreateDTOValidator chatCreateDTOValidator;

    public ChatController(
        ChatRepository chatRepository,
        UserRepository userRepository,
        ChatCreateDTOValidator chatCreateDTOValidator
    ) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.chatCreateDTOValidator = chatCreateDTOValidator;
    }

    @PostMapping
    @Transactional
    public RestResponse<Integer> create(
        @RequestBody @Valid ChatCreateDTO chatCreateDTO, BindingResult errors,
        @AuthenticationPrincipal User currentUser
    ) throws MethodArgumentNotValidException {
        chatCreateDTOValidator.validate(chatCreateDTO, errors);
        if (errors.hasErrors()) {
            try {
                throw new MethodArgumentNotValidException(
                    new MethodParameter(
                        ChatController.class.getDeclaredMethod(
                            "create",
                            ChatCreateDTO.class,
                            BindingResult.class,
                            User.class
                    ), 0
                    ), errors
                );
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Unexpected exception: controller method not found");
            }
        }

        var usernames = chatCreateDTO.usernames();

        var chat = new Chat();
        chat.setUsers(new HashSet<>());
        chat.setName(chatCreateDTO.chatName());

        usernames.add(currentUser.getUsername());
        for (var username : usernames) {
            var user = userRepository.findByUsername(username)
                                     .orElseThrow(() -> new RuntimeException("Validator of chat dto didn't work"));
            chat.getUsers().add(user);
        }

        chat = chatRepository.save(chat);

        for (User user : chat.getUsers()) {
            if (user.getChats() == null) {
                user.setChats(new HashSet<>());
            }
            user.getChats().add(chat);
            userRepository.save(user);
        }

        return RestResponse.created(chat.getId());
    }

    @GetMapping("/{id}")
    public RestResponse<ReadChatDTO> read(@PathVariable int id, @AuthenticationPrincipal User currentUser) {
        currentUser = userRepository.findById(currentUser.getId()).orElse(null);
        if (currentUser == null) {
            throw new RuntimeException("Current logged-in user not found in repository");
        }

        var requestedChatOpt = chatRepository.findById(id);
        if (requestedChatOpt.isPresent()) {
            var requestedChat = requestedChatOpt.get();
            System.out.println(currentUser.getChats());
            if (currentUser.getChats().contains(requestedChat)) {
                var messages = requestedChat.getMessages()
                                            .stream()
                                            .map(m -> new ReadChatMessageDTO(
                                                m.getContent(),
                                                m.getSender().getUsername(),
                                                m.getCreatedAt()
                                            ))
                                            .toList();
                var dto = new ReadChatDTO(requestedChat.getName(), messages);
                return new RestResponse<>(200, "Success", dto);
            }
        }

        return new RestResponse<>(403, "You don't have access to this chat", null);
    }

    @GetMapping("/{id}/users")
    public RestResponse<List<ListChatUsersUserDTO>> listChatUsers(
        @PathVariable int id,
        @AuthenticationPrincipal User currentUser
    ) {
        currentUser = userRepository.findById(currentUser.getId()).orElse(null);
        if (currentUser == null) {
            throw new RuntimeException("Current logged-in user not found in repository");
        }

        var requestedChatOpt = chatRepository.findById(id);
        if (requestedChatOpt.isPresent()) {
            var requestedChat = requestedChatOpt.get();
            if (currentUser.getChats().contains(requestedChat)) {
                var users = requestedChat.getUsers()
                                         .stream()
                                         .map(user -> new ListChatUsersUserDTO(user.getId(), user.getUsername()))
                                         .toList();
                return new RestResponse<>(200, "Success", users);
            }
        }

        return new RestResponse<>(403, "You don't have access to this chat", null);
    }
}
