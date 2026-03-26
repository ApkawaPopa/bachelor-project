package minchakov.arkadii.amina.controller.advice;

import jakarta.validation.ValidationException;
import minchakov.arkadii.amina.dto.RestResponse;
import minchakov.arkadii.amina.dto.StompResponse;
import minchakov.arkadii.amina.exception.AppException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
    public RestResponse<Void> handleValidationErrors(ValidationException e) {
        e.printStackTrace();
        return new RestResponse<>(400, "Validation failed: " + e.getMessage());
    }

    @ExceptionHandler
    public RestResponse<Void> handleAppException(AppException e) {
        return new RestResponse<>(e.getStatus().value(), e.getMessage());
    }

    @ExceptionHandler
    public RestResponse<Void> handleAuthenticationException(AuthenticationException e) {
        return new RestResponse<>(401, e.getMessage());
    }

    @ExceptionHandler
    public RestResponse<Void> handleAccessDenied(AccessDeniedException e) {
        return new RestResponse<>(403, e.getMessage());
    }

    @ExceptionHandler
    public RestResponse<Void> handleResourceNotFound(NoResourceFoundException e) {
        return new RestResponse<>(404, e.getMessage());
    }

    @ExceptionHandler
    public RestResponse<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return new RestResponse<>(400, "Malformed request: " + e.getMessage());
    }

    @ExceptionHandler
    public RestResponse<Void> handleUnexpectedException(Exception e) {
        e.printStackTrace();
        return new RestResponse<>(500, "Internal Server Error: " + e.getMessage());
    }

    @MessageExceptionHandler
    @SendToUser("/queue/error")
    public StompResponse<Map<String, List<String>>> handleMessageValidationErrors(org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException e) {
        Map<String, List<String>> errors = e.getBindingResult().getFieldErrors().stream().collect(Collectors.groupingBy(
            FieldError::getField,
            Collectors.mapping(DefaultMessageSourceResolvable::getDefaultMessage, Collectors.toList())
        ));
        return new StompResponse<>(400, "Validation failed", errors);
    }

    @MessageExceptionHandler
    @SendToUser("/queue/error")
    public StompResponse<Void> handleMessageAppException(AppException e) {
        return new StompResponse<>(e.getStatus().value(), e.getMessage());
    }

    @MessageExceptionHandler
    @SendToUser("/queue/error")
    public StompResponse<Void> handleMessageAccessDeniedException(AccessDeniedException e) {
        return new StompResponse<>(403, "Access denied: " + e.getMessage());
    }

    @MessageExceptionHandler
    @SendToUser("/queue/error")
    public StompResponse<Void> handleMessageUnexpectedException(Exception e) {
        System.out.println("What exception what: " + e.getClass());
        e.printStackTrace();
        return new StompResponse<>(500, "Internal server error: " + e.getMessage());
    }
}
