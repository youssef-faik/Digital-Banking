package com.example.digitalbanking.exceptions;

public class CustomerDeletionException extends RuntimeException {
    public CustomerDeletionException(String message) {
        super(message);
    }
}