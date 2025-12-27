package minchakov.arkadii.amina.dto;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

public class ApiResponse<T> extends ResponseEntity<ApiResponseBody<T>> {

    public ApiResponse(int code, String message, T data) {
        super(new ApiResponseBody<>(code, message, data), HttpStatusCode.valueOf(code));
    }
}
