package com.example.demo.unit.service;

import com.example.demo.exception.DailyBorrowLimitExceededException;
import com.example.demo.exception.UnreturnedBorrowExistsException;
import com.example.demo.policy.BorrowPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class BorrowPolicyTest {

    private BorrowPolicy borrowPolicy;

    @BeforeEach
    void setUp() {
        borrowPolicy = new BorrowPolicy();
    }

    @Test
    void validateNoActiveBorrow_should_not_throw_when_no_active_borrow() {
        assertDoesNotThrow(() -> borrowPolicy.validateNoActiveBorrow(false));
    }

    @Test
    void validateNoActiveBorrow_should_throw_when_active_borrow_exists() {
        assertThrows(UnreturnedBorrowExistsException.class, () -> borrowPolicy.validateNoActiveBorrow(true));
    }

    @Test
    void validateDailyLimit_should_not_throw_when_member_has_no_previous_borrow() {
        assertDoesNotThrow(() -> borrowPolicy.validateDailyLimit(false, false));
    }

    @Test
    void validateDailyLimit_should_not_throw_when_limit_not_reached() {
        assertDoesNotThrow(() -> borrowPolicy.validateDailyLimit(true, false));
    }

    @Test
    void validateDailyLimit_should_not_throw_when_exists_is_false_even_if_limit_reached() {
        assertDoesNotThrow(() -> borrowPolicy.validateDailyLimit(false, true));
    }

    @Test
    void validateDailyLimit_should_throw_when_daily_limit_reached() {
        assertThrows(DailyBorrowLimitExceededException.class, () -> borrowPolicy.validateDailyLimit(true, true));
    }
}