package minchakov.arkadii.amina.dto;

import java.time.LocalDateTime;

public record StompResponse<T>(
    int code, String message, T data, LocalDateTime timestamp
) {
    public StompResponse(int code, String message) {
        this(code, message, null, LocalDateTime.now());
    }

    public StompResponse(int code, String message, T data) {
        this(code, message, data, LocalDateTime.now());
    }

    public static <T> StompResponse<T> success(T data) {
        return new StompResponse<>(200, "Success", data, LocalDateTime.now());
    }
}
