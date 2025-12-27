package minchakov.arkadii.amina.exception;

import org.springframework.validation.BindingResult;

public class DtoValidationException extends RuntimeException {
    private final BindingResult result;

    public DtoValidationException(BindingResult result) {
        this.result = result;
    }

    public BindingResult getResult() {
        return result;
    }
}
