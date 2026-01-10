package minchakov.arkadii.amina.exception;

import org.springframework.http.HttpStatus;

public class InternalServerErrorException extends AppException {
    public InternalServerErrorException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}
