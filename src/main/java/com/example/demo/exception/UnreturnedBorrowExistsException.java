package com.example.demo.exception;

public class UnreturnedBorrowExistsException extends RuntimeException {
    public UnreturnedBorrowExistsException() {
        super("まだ貸出返却されていないの貸し出しがあります。");
    }
}
