package com.jm.exceptions;

public class InvalidKeyException extends RuntimeException {

    public InvalidKeyException(String msg) {
        super(msg);
    }

    public InvalidKeyException() {
        super();
    }
}
