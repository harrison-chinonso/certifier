package com.line.medusa_merchant.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UpdateExceptions extends RuntimeException{

    private static final long serialVersionUID = 7222147487442383423L;

    private String responseCode;

    private HttpStatus status;

    public UpdateExceptions(String message) {
        super(message);
    }

    public UpdateExceptions(String message, String responseCode, HttpStatus status){
        super(message);
        this.responseCode = responseCode;
        this.status = status;
    }
}
