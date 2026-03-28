package minchakov.arkadii.amina.controller;

import jakarta.validation.Valid;
import minchakov.arkadii.amina.dto.ChatCreateDTO;
import minchakov.arkadii.amina.dto.ListChatUsersUserDTO;
import minchakov.arkadii.amina.dto.ReadChatDTO;
import minchakov.arkadii.amina.dto.ReadChatMessageDTO;
import minchakov.arkadii.amina.dto.RestResponse;
import minchakov.arkadii.amina.model.User;
import minchakov.arkadii.amina.model.UserChat;
import minchakov.arkadii.amina.service.ChatService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public RestResponse<Integer> create(@RequestBody @Valid ChatCreateDTO chatCreateDTO) {
        var chat = chatService.createChat(chatCreateDTO);
        return RestResponse.created(chat.getId());
    }

    @GetMapping("/{id}")
    public RestResponse<ReadChatDTO> read(@PathVariable int id, @AuthenticationPrincipal User currentUser) {
        var requestedChat = chatService.getChat(id, currentUser);

        var messages = requestedChat.getMessages()
                                    .stream()
                                    .map(m -> new ReadChatMessageDTO(
                                        m.getId(),
                                        m.getContent(),
                                        m.getSender().getUsername(),
                                        m.getCreatedAt()
                                    ))
                                    .toList();

        var dto = new ReadChatDTO(requestedChat.getName(), messages);
        return RestResponse.success(dto);
    }

    @DeleteMapping("/{id}")
    public RestResponse<Integer> delete(@PathVariable int id, @AuthenticationPrincipal User currentUser) {
        return RestResponse.success(chatService.deleteChat(id, currentUser));
    }

    @GetMapping("/{id}/users")
    public RestResponse<List<ListChatUsersUserDTO>> listChatUsers(
        @PathVariable int id,
        @AuthenticationPrincipal User currentUser
    ) {
        Set<UserChat> chatUsers = chatService.getChatUsers(id, currentUser);
        var data = chatUsers.stream()
                            .map(chatUser -> new ListChatUsersUserDTO(
                                chatUser.getUser().getId(),
                                chatUser.getUser().getUsername()
                            ))
                            .toList();
        return RestResponse.success(data);
    }

    @GetMapping("/{id}/key")
    public RestResponse<String> getEncryptedSymmetricKey(
        @PathVariable int id,
        @AuthenticationPrincipal User currentUser
    ) {
        UserChat chatUser = chatService.getChatUser(id, currentUser);
        var encryptedSymmetricKey = chatUser.getEncryptedSymmetricKey();
        return RestResponse.success(encryptedSymmetricKey);
    }
}
