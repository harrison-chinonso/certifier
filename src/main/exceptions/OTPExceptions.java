package com.line.medusa_merchant.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OTPExceptions extends RuntimeException {

    private static final long serialVersionUID = 8689863155281616150L;

    private String responseCode;
    private HttpStatus status;


    public OTPExceptions(String message) {
        super(message);
    }

    public OTPExceptions(String message, String responseCode, HttpStatus status) {
        super(message);
        this.responseCode = responseCode;
        this.status = status;
    }
}
