package minchakov.arkadii.amina.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import minchakov.arkadii.amina.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class GetUsersKeysUsernameValidator implements ConstraintValidator<GetUsersKeysUsernameConstraint, String> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean isValid(String username, ConstraintValidatorContext constraintValidatorContext) {
        return userRepository.existsByUsername(username);
    }
}
