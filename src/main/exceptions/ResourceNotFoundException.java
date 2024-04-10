/*
 * *
 *  * Created by Kolawole Omirin
 *  * Copyright (c) 2023 . All rights reserved.
 *  * Last modified 11/15/23, 3:19 PM
 *
 */

package com.line.medusa_merchant.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 5067211343246617692L;

    private String responseCode;

    private HttpStatus status;


    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, String responseCode, HttpStatus status) {
        super(message);
        this.responseCode = responseCode;
        this.status = status;
    }
}
