package com.example.demo.exception;

public class DailyBorrowLimitExceededException extends RuntimeException {
    public DailyBorrowLimitExceededException(String errorMessage) {
        super(errorMessage);
    }
}
