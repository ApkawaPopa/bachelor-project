package minchakov.arkadii.amina.validator;

import minchakov.arkadii.amina.dto.ChatCreateDTO;
import minchakov.arkadii.amina.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ChatCreateDTOValidator implements Validator {

    private final UserRepository userRepository;

    public ChatCreateDTOValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return ChatCreateDTO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        var dto = (ChatCreateDTO) target;
        for (var username : dto.usernames()) {
            if (userRepository.findByUsername(username).isEmpty()) {
                errors.rejectValue("usernames", "", "Username '" + username + "' not found");
            }
        }
    }
}
