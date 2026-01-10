package minchakov.arkadii.amina.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ChatCreateOwnUsernameValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ChatCreateOwnUsernameConstraint {
    String message() default "Cannot create chat without authenticated user";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
