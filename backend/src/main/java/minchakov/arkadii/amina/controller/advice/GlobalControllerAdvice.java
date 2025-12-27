package minchakov.arkadii.amina.controller.advice;

import minchakov.arkadii.amina.controller.AuthController;
import minchakov.arkadii.amina.dto.ApiResponse;
import minchakov.arkadii.amina.exception.DtoValidationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@ControllerAdvice(basePackageClasses = AuthController.class)
public class GlobalControllerAdvice {

    @ExceptionHandler(DtoValidationException.class)
    public ApiResponse<HashMap<String, List<String>>> handleDtoValidationException(DtoValidationException e) {
        var bindingResult = e.getResult();
        var fieldToErrors = new HashMap<String, List<String>>();
        bindingResult.getFieldErrors().forEach(fieldError -> {
            var list = fieldToErrors.computeIfAbsent(fieldError.getField(), _ -> new ArrayList<>());
            list.add(fieldError.getDefaultMessage());
        });
        return new ApiResponse<>(400, "Error while validating fields of passed object", fieldToErrors);
    }
}
