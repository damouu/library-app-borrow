package com.example.demo.exception;

public class DailyBorrowLimitExceededException extends RuntimeException {
    public DailyBorrowLimitExceededException() {
        super("一日の借入限度額に達しました。");
    }
}
