package minchakov.arkadii.amina.dto;

import java.time.LocalDateTime;

public record RestResponseBody<T>(
    int code, String message, T data, LocalDateTime timestamp
) {
}
