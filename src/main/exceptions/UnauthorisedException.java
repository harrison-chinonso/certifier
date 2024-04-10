package com.line.medusa_merchant.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UnauthorisedException extends RuntimeException {
    private static final long serialVersionUID = 2982216599071591482L;

    private String responseCode;

    private HttpStatus status;


    public UnauthorisedException(String message) {
        super(message);
        status = HttpStatus.UNAUTHORIZED;
    }

    public UnauthorisedException(String message, String responseCode, HttpStatus status) {
        super(message);
        this.responseCode = responseCode;
        this.status = status;
    }
}
