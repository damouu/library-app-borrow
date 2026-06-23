package com.example.demo.handler;

import com.example.demo.exception.DailyBorrowLimitExceededException;
import com.example.demo.exception.UnreturnedBorrowExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnreturnedBorrowExistsException.class)
    public ResponseEntity<?> handleActiveBorrow(UnreturnedBorrowExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(DailyBorrowLimitExceededException.class)
    public ResponseEntity<?> handleLimit(DailyBorrowLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }
}