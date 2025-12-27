package minchakov.arkadii.amina.dto;

public record ApiResponseBody<T>(int code, String message, T data) {

}
