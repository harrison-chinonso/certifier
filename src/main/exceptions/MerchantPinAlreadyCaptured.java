package com.line.medusa_merchant.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class MerchantPinAlreadyCaptured extends RuntimeException{

    private static final long serialVersionUID = -4211136788717092058L;

    private String responseCode;

    private HttpStatus status;



    public MerchantPinAlreadyCaptured(String message) {
        super(message);
    }

    public MerchantPinAlreadyCaptured(String message, String responseCode, HttpStatus status){
        super(message);
        this.responseCode = responseCode;
        this.status = status;
    }
}
