package minchakov.arkadii.amina.controller;

import minchakov.arkadii.amina.dto.ApiResponse;
import minchakov.arkadii.amina.dto.ListUserChatsChatDTO;
import minchakov.arkadii.amina.model.User;
import minchakov.arkadii.amina.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@Transactional
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/chats")
    public ApiResponse<List<ListUserChatsChatDTO>> listChatsByUserId(@AuthenticationPrincipal User currentUser) {

        var userOpt = userRepository.findById(currentUser.getId());
        if (userOpt.isEmpty()) {
            return new ApiResponse<>(500, "Cannot find authenticated user", null);
        }

        var user = userOpt.get();

        // TODO: добавить получение последнего сообщения
        var chats = user.getChats()
                        .stream()
                        .map(chat -> new ListUserChatsChatDTO(chat.getId(), chat.getName(), null))
                        .toList();

        return new ApiResponse<>(200, "Success", chats);
    }
}
