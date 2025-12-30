package minchakov.arkadii.amina.dto;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

public class RestResponse<T> extends ResponseEntity<RestResponseBody<T>> {

    public RestResponse(int code, String message, T data) {
        super(new RestResponseBody<>(code, message, data), HttpStatusCode.valueOf(code));
    }

    public RestResponse(int code, String message) {
        super(new RestResponseBody<>(code, message, null), HttpStatusCode.valueOf(code));
    }

    public static <T> RestResponse<T> success(T data) {
        return new RestResponse<>(200, "Success", data);
    }

    public static <T> RestResponse<T> created(T data) {
        return new RestResponse<>(201, "Created", data);
    }
}
