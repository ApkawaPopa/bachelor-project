package minchakov.arkadii.amina.exception;

import org.springframework.validation.BindingResult;

public class DtoValidationException extends RuntimeException {
    private final BindingResult result;

    public DtoValidationException(BindingResult errors) {
        this.result = errors;
    }

    public BindingResult getResult() {
        return result;
    }
}
