package com.line.medusa_merchant.exceptions;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class WalletCreationException extends RuntimeException{
    private static final long serialVersionUID = 3109203809065779891L;

    private String responseCode;

    private HttpStatus status;


    public WalletCreationException(String message) {
        super(message);
    }

    public WalletCreationException(String message, String responseCode, HttpStatus status){
        super(message);
        this.responseCode = responseCode;
        this.status = status;
    }
}
