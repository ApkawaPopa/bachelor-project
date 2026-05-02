package minchakov.arkadii.amina.controller;

import jakarta.validation.Valid;
import minchakov.arkadii.amina.dto.GetUsersKeysInDTO;
import minchakov.arkadii.amina.dto.GetUsersKeysOutDTO;
import minchakov.arkadii.amina.dto.ListUserChatsChatDTO;
import minchakov.arkadii.amina.dto.RestResponse;
import minchakov.arkadii.amina.model.User;
import minchakov.arkadii.amina.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;
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
        return RestResponse.success(chats);
    }

    @PostMapping("/keys")
    public RestResponse<List<GetUsersKeysOutDTO>> getUsersKeys(@Valid @RequestBody GetUsersKeysInDTO inDto) {
        var users = userService.getUsersKeys(inDto.usernames());
        return RestResponse.success(users);
    }

    @GetMapping("/{username}/pictures")
    public RestResponse<List<URL>> getProfilePictures(@PathVariable String username) {
        return RestResponse.success(userService.getProfilePictures(username));
    }

    @PostMapping("/pictures")
    public RestResponse<URL> setProfilePicture(
        @RequestBody int pictureObjectId,
        @AuthenticationPrincipal User currentUser
    ) {
        return RestResponse.success(userService.setProfilePicture(pictureObjectId, currentUser));
    }
}
