package minchakov.arkadii.amina.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import minchakov.arkadii.amina.dto.ChatCreateUserDetailsDTO;
import minchakov.arkadii.amina.exception.InternalServerErrorException;
import minchakov.arkadii.amina.model.User;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

public class ChatCreateOwnUsernameValidator
    implements ConstraintValidator<ChatCreateOwnUsernameConstraint, List<ChatCreateUserDetailsDTO>> {

    @Override
    public boolean isValid(
        List<ChatCreateUserDetailsDTO> chatCreateUserDetailsDTOS,
        ConstraintValidatorContext constraintValidatorContext
    ) {
        try {
            var currentUserUsername = ((User) SecurityContextHolder.getContext()
                                                                   .getAuthentication()
                                                                   .getPrincipal()).getUsername();
            return chatCreateUserDetailsDTOS.contains(new ChatCreateUserDetailsDTO(currentUserUsername, null));
        } catch (RuntimeException e) {
            throw new InternalServerErrorException("Authentication not found");
        }
    }
}
