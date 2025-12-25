package minchakov.arkadii.amina.controller;

import jakarta.transaction.Transactional;
import minchakov.arkadii.amina.dto.ApiResponse;
import minchakov.arkadii.amina.dto.ChatCreateDTO;
import minchakov.arkadii.amina.model.Chat;
import minchakov.arkadii.amina.repository.ChatRepository;
import minchakov.arkadii.amina.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.NoSuchElementException;

@Controller
@Transactional
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    public ChatController(ChatRepository chatRepository, UserRepository userRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ApiResponse<Integer> create(@RequestBody ChatCreateDTO chatCreateDTO) {
        // TODO: Валидация
        var chat = chatRepository.save(new Chat());
        for (var username : chatCreateDTO.getUsernames()) {
            var userOpt = userRepository.findByUsername(username);
            try {
                var user = userOpt.get();
                chat.getUsers().add(user);
                user.getChats().add(chat);
            } catch (NoSuchElementException e) {
                throw new RuntimeException("Нужно добавить валидацию", e);
            }
        }
        return new ApiResponse<>(201, "Created", chat.getId());
    }
}
