package minchakov.arkadii.amina.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ChatCreateUsernameValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ChatCreateUsernameConstraint {
    String message() default "Username doesn't exist";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
