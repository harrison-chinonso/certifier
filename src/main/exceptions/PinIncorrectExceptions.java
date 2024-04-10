package com.line.medusa_merchant.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PinIncorrectExceptions extends RuntimeException{
    private static final long serialVersionUID = 8589601677893112777L;

    private String responseCode;

    private HttpStatus status;


    public PinIncorrectExceptions(String message) {
        super(message);
    }

    public PinIncorrectExceptions(String message, String responseCode, HttpStatus status){
        super(message);
        this.responseCode = responseCode;
        this.status = status;
    }
}
