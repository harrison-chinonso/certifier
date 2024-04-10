package com.line.medusa_merchant.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GeneralException extends RuntimeException {

    private static final long serialVersionUID = -4967588218802689301L;

    private String responseCode;
    private HttpStatus status;



    public GeneralException() {
        super();
    }

    public GeneralException(String message) {
        super(message);
    }

    public GeneralException(String message, String responseCode, HttpStatus status) {
        super(message);
        this.responseCode = responseCode;
        this.status = status;
    }
}
