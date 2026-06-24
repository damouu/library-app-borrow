package com.example.demo.unit.service;

import com.example.demo.exception.BorrowNotFoundException;
import com.example.demo.exception.DailyBorrowLimitExceededException;
import com.example.demo.exception.UnreturnedBorrowExistsException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TestExceptionController {

    @GetMapping("/test/conflict")
    void conflict() {
        throw new UnreturnedBorrowExistsException("Conflict error");
    }

    @GetMapping("/test/forbidden")
    void forbidden() {
        throw new DailyBorrowLimitExceededException("Limit exceeded");
    }

    @GetMapping("/test/notfound")
    void notFound() {
        throw new BorrowNotFoundException("Not found");
    }
}