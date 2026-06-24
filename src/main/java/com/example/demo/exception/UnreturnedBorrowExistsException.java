package com.example.demo.exception;

public class UnreturnedBorrowExistsException extends RuntimeException {
    public UnreturnedBorrowExistsException(String errorMessage) {
        super(errorMessage);
    }
}
