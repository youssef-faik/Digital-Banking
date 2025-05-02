package com.example.digitalbanking.exceptions;

// Changed to extend RuntimeException (unchecked)
public class BankAccountNotFoundException extends RuntimeException {
    public BankAccountNotFoundException(String message) {
        super(message);
    }
}
