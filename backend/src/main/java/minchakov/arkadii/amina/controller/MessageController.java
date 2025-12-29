package minchakov.arkadii.amina.controller;

import jakarta.validation.Valid;
import minchakov.arkadii.amina.dto.ApiResponse;
import minchakov.arkadii.amina.dto.GetMessageDTO;
import minchakov.arkadii.amina.dto.UpdateMessageDTO;
import minchakov.arkadii.amina.exception.DtoValidationException;
import minchakov.arkadii.amina.model.User;
import minchakov.arkadii.amina.service.MessageService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/chat/{chatId}/message")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public ApiResponse<List<GetMessageDTO>> list(@PathVariable int chatId, @AuthenticationPrincipal User user) {
        return new ApiResponse<>(200, "Success", messageService.listMessages(chatId, user));
    }

    @PatchMapping("/{id}")
    public ApiResponse<LocalDateTime> update(
        @RequestBody @Valid UpdateMessageDTO updateMessageDTO,
        BindingResult errors,
        @PathVariable int chatId,
        @PathVariable int id,
        @AuthenticationPrincipal User user
    ) {
        if (errors.hasErrors()) {
            throw new DtoValidationException(errors);
        }

        var updatedAt = messageService.updateMessage(updateMessageDTO, chatId, id, user);

        return new ApiResponse<>(200, "Success", updatedAt);
    }
}
