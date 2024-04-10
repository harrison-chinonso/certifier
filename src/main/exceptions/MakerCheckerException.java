package com.line.medusa_merchant.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class MakerCheckerException extends RuntimeException {
    private static final long serialVersionUID = -3289122662320115495L;

    private String responseCode;

    private HttpStatus status;



    public MakerCheckerException(String approval_type_cannot_be_null) {
        super(approval_type_cannot_be_null);
    }

    public MakerCheckerException(String message, String responseCode, HttpStatus status){
        super(message);
        this.responseCode = responseCode;
        this.status = status;
    }

}
