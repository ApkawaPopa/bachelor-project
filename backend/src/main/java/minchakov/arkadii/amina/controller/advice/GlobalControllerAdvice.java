package minchakov.arkadii.amina.controller.advice;

import minchakov.arkadii.amina.dto.RestResponse;
import minchakov.arkadii.amina.dto.StompResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler
    public RestResponse<Map<String, List<String>>> handleValidationErrors(MethodArgumentNotValidException e) {
        Map<String, List<String>> errors = e.getBindingResult().getFieldErrors().stream().collect(Collectors.groupingBy(
            FieldError::getField,
            Collectors.mapping(DefaultMessageSourceResolvable::getDefaultMessage, Collectors.toList())
        ));
        return new RestResponse<>(400, "Validation failed", errors);
    }

    @ExceptionHandler
    public RestResponse<Void> handleAuthenticationException(AuthenticationException e) {
        return new RestResponse<>(403, e.getMessage());
    }

    @ExceptionHandler
    public RestResponse<Void> handleUnexpectedException(Exception e) {
        e.printStackTrace();
        return new RestResponse<>(500, "Internal Server Error: " + e.getMessage());
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public StompResponse<Map<String, List<String>>> handleMessageValidationErrors(MethodArgumentNotValidException e) {
        Map<String, List<String>> errors = e.getBindingResult().getFieldErrors().stream().collect(Collectors.groupingBy(
            FieldError::getField,
            Collectors.mapping(DefaultMessageSourceResolvable::getDefaultMessage, Collectors.toList())
        ));
        return new StompResponse<>(400, "Validation failed", errors);
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public StompResponse<Void> handleMessageAccessDeniedException(AccessDeniedException e) {
        return new StompResponse<>(403, "Access denier: " + e.getMessage());
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public StompResponse<Void> handleMessageUnexpectedException(Exception e) {
        e.printStackTrace();
        return new StompResponse<>(500, "Internal server error: " + e.getMessage());
    }
}
