package minchakov.arkadii.amina.dto;

public record StompResponse<T>(
    int code, String message, T data
) {
    public StompResponse(int code, String message) {
        this(code, message, null);
    }

    public static <T> StompResponse<T> success(T data) {
        return new StompResponse<>(200, "Success", data);
    }

    public static <T> StompResponse<T> error(int code, String message) {
        return new StompResponse<>(code, message, null);
    }
}
