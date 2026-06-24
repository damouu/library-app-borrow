package com.example.demo.exception;

public class BorrowNotFoundException extends RuntimeException {
    public BorrowNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
