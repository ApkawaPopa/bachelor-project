package minchakov.arkadii.amina.dto;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

public class ApiResponseEntity<T> extends ResponseEntity<ApiResponse<T>> {

    public ApiResponseEntity(int code, String message, T data) {
        super(new ApiResponse<>(code, message, data), HttpStatusCode.valueOf(code));
    }
}
