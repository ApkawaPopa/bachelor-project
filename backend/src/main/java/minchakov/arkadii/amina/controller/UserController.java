package minchakov.arkadii.amina.controller;

import minchakov.arkadii.amina.dto.ListUserChatsChatDTO;
import minchakov.arkadii.amina.dto.RestResponse;
import minchakov.arkadii.amina.model.User;
import minchakov.arkadii.amina.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/chats")
    public RestResponse<List<ListUserChatsChatDTO>> listCurrentUserChats(@AuthenticationPrincipal User currentUser) {
        var chats = userService.listUserChats(currentUser.getId());
        return new RestResponse<>(200, "Success", chats);
    }
}
