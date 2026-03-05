package fitSnap.product_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized Exception", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "INVALID_KEY", HttpStatus.BAD_REQUEST),
    USER_EXISITED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "User name must be as least{min}", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "User name must be as least{min}", HttpStatus.BAD_REQUEST),
    USER_NOTEXISITED(1005, " User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unathenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You dont have permission ", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be as least{min}", HttpStatus.UNAUTHORIZED),
    FILE_NOT_FOUND(1009, "Your file name not found", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND(1010, "Product not found", HttpStatus.NOT_FOUND),
    FILE_UPLOAD_FAILED(1011, "File upload failed", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_FILE_TYPE(1012, "Invalid file type", HttpStatus.BAD_REQUEST);

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }

    private int code;
    private String message;
    private HttpStatusCode httpStatusCode;
}
