package com.uet.legacy.validation;

public class InvalidOrderException extends Exception {
    public InvalidOrderException(String message) {
        super(message);
    }
}