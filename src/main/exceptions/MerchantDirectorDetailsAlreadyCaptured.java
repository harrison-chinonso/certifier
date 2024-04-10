package com.line.medusa_merchant.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class MerchantDirectorDetailsAlreadyCaptured extends RuntimeException{
    private static final long serialVersionUID = 6234134660106241446L;

    private String responseCode;

    private HttpStatus status;



    public MerchantDirectorDetailsAlreadyCaptured(String message) {
        super(message);
    }

    public MerchantDirectorDetailsAlreadyCaptured(String message, String responseCode, HttpStatus status){
        super(message);
        this.responseCode = responseCode;
        this.status = status;
    }
}
