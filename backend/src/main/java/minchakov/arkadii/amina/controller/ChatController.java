package minchakov.arkadii.amina.controller;

import jakarta.validation.Valid;
import minchakov.arkadii.amina.dto.ApiResponse;
import minchakov.arkadii.amina.dto.ChatCreateDTO;
import minchakov.arkadii.amina.dto.ListChatUsersUserDTO;
import minchakov.arkadii.amina.dto.ReadChatDTO;
import minchakov.arkadii.amina.dto.ReadChatMessageDTO;
import minchakov.arkadii.amina.exception.DtoValidationException;
import minchakov.arkadii.amina.model.Chat;
import minchakov.arkadii.amina.model.User;
import minchakov.arkadii.amina.repository.ChatRepository;
import minchakov.arkadii.amina.repository.UserRepository;
import minchakov.arkadii.amina.validator.ChatCreateDTOValidator;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashSet;
import java.util.List;

@Controller
@Transactional
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final ChatCreateDTOValidator chatCreateDTOValidator;

    public ChatController(
        ChatRepository chatRepository,
        UserRepository userRepository,
        ModelMapper modelMapper,
        ChatCreateDTOValidator chatCreateDTOValidator
    ) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.chatCreateDTOValidator = chatCreateDTOValidator;
    }

    @PostMapping
    @Transactional
    public ApiResponse<Integer> create(
        @RequestBody @Valid ChatCreateDTO chatCreateDTO,
        BindingResult result,
        @AuthenticationPrincipal User currentUser
    ) {
        chatCreateDTOValidator.validate(chatCreateDTO, result);

        if (result.hasErrors()) {
            throw new DtoValidationException(result);
        }

        // TODO: поменять логику заполнения имени чата
        var usernames = chatCreateDTO.getUsernames();

        Chat chat = new Chat();
        chat.setUsers(new HashSet<>());

        if (usernames.size() == 1) {
            chat.setName(usernames.stream()
                                  .findAny()
                                  .orElseThrow(() -> new RuntimeException(
                                      "Impossible exception while searching element in one-element array")));
        } else {
            chat.setName(chatCreateDTO.getChatName());
        }

        usernames.add(currentUser.getUsername());
        for (String username : usernames) {
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

        return new ApiResponse<>(201, "Чат успешно создан", chat.getId());
    }

    @GetMapping("/{id}")
    public ApiResponse<ReadChatDTO> read(@PathVariable int id, @AuthenticationPrincipal User currentUser) {
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
                                            .map(m -> modelMapper.map(m, ReadChatMessageDTO.class))
                                            .toList();
                var dto = new ReadChatDTO(requestedChat.getName(), messages);
                return new ApiResponse<>(200, "Success", dto);
            }
        }

        return new ApiResponse<>(403, "You don't have access to this chat", null);
    }

    @GetMapping("/{id}/users")
    public ApiResponse<List<ListChatUsersUserDTO>> listChatUsers(
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
                                         .map(user -> modelMapper.map(user, ListChatUsersUserDTO.class))
                                         .toList();
                return new ApiResponse<>(200, "Success", users);
            }
        }

        return new ApiResponse<>(403, "You don't have access to this chat", null);
    }
}
