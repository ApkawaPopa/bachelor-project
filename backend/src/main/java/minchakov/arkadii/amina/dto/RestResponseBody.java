package minchakov.arkadii.amina.dto;

public record RestResponseBody<T>(
    int code, String message, T data
) {
}
