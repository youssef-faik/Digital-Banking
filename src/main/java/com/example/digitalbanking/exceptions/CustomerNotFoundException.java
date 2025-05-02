package com.example.digitalbanking.exceptions;

// Changed to extend RuntimeException (unchecked)
public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
