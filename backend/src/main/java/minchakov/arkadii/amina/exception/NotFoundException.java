package minchakov.arkadii.amina.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends AppException {

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public static NotFoundException stompDefault() {
        return new NotFoundException("Invalid destination");
    }
}
