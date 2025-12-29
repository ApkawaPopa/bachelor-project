package minchakov.arkadii.amina.controller.advice;

import jakarta.servlet.http.HttpServletRequest;
import minchakov.arkadii.amina.dto.ApiResponse;
import minchakov.arkadii.amina.dto.StompResponse;
import minchakov.arkadii.amina.exception.DtoValidationException;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@ControllerAdvice(basePackages = {"minchakov.arkadii.amina.controller"})
public class GlobalControllerAdvice {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public GlobalControllerAdvice(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @ExceptionHandler
    public ApiResponse<HashMap<String, List<String>>> handleDtoValidationException(DtoValidationException e) {
        var bindingResult = e.getResult();
        var fieldToErrors = new HashMap<String, List<String>>();
        bindingResult.getFieldErrors().forEach(fieldError -> {
            var list = fieldToErrors.computeIfAbsent(fieldError.getField(), _ -> new ArrayList<>());
            list.add(fieldError.getDefaultMessage());
        });
        return new ApiResponse<>(400, "Error while validating fields of passed object", fieldToErrors);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(HttpServletRequest request, Exception e) {
        return new ApiResponse<>(
            500,
            "Unexpected exception at " + request.getServletPath() + " " + e.getMessage(),
            null
        );
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public StompResponse<Message<?>> handleException(
        Exception e,
        Message<?> message,
        @Header("destination") String destination
    ) {
        return new StompResponse<>(
            500,
            "Unexpected exception at destination '" + destination + "': " + e.getMessage(),
            message
        );
    }

}
