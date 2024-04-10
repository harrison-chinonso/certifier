package org.dominion.city.dca.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DuplicateEntityException extends RuntimeException{
    private static final long serialVersionUID = 7101434904253494890L;

    private String responseCode;

    private HttpStatus status;

    public DuplicateEntityException(String s){
        super(s);
    }

    public DuplicateEntityException(String message, String responseCode, HttpStatus status){
        super(message);
        this.responseCode = responseCode;
        this.status = status;
    }
}
