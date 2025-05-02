package com.example.digitalbanking.exceptions;

// Changed to extend RuntimeException (unchecked)
public class BalanceNotSufficientException extends RuntimeException {
    public BalanceNotSufficientException(String message) {
        super(message);
    }
}
