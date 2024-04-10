package com.line.medusa_merchant.exceptions;

public class SpringSecurityException extends RuntimeException{
    private static final long serialVersionUID = 3150456347060415248L;

    public SpringSecurityException(String message) {
        super(message);
    }
}
