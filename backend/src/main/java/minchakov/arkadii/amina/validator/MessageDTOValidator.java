package minchakov.arkadii.amina.validator;

import minchakov.arkadii.amina.dto.MessageDTO;
import minchakov.arkadii.amina.repository.ChatRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class MessageDTOValidator implements Validator {

    private final Validator validator;
    private final ChatRepository chatRepository;

    public MessageDTOValidator(@Qualifier("defaultValidator") Validator validator, ChatRepository chatRepository) {
        this.validator = validator;
        this.chatRepository = chatRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return MessageDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validator.validate(target, errors);

        var dto = (MessageDTO) target;
        if (chatRepository.findById(dto.chatId()).isEmpty()) {
            errors.rejectValue("chatId", "", "Chat not found by id: " + dto.chatId());
        }
    }
}
