package com.example.demo.unit.service;

import com.example.demo.exception.UnreturnedBorrowExistsException;
import com.example.demo.model.Borrow;
import com.example.demo.policy.ReturnBorrowPolicy;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReturnBorrowPolicyTest {

    private ReturnBorrowPolicy returnBorrowPolicy;

    @BeforeEach
    void setUp() {
        returnBorrowPolicy = new ReturnBorrowPolicy();
    }

    @Test
    void validateAndGetBorrow_should_return_first_borrow() {

        Borrow borrow = Instancio.create(Borrow.class);

        List<Borrow> borrows = List.of(borrow);

        Borrow result = returnBorrowPolicy.validateAndGetBorrow(borrows);

        assertThat(result).isEqualTo(borrow);
    }

    @Test
    void validateAndGetBorrow_should_throw_when_list_is_empty() {

        assertThrows(UnreturnedBorrowExistsException.class, () -> returnBorrowPolicy.validateAndGetBorrow(List.of()));
    }

    @Test
    void calculateDaysLate_should_return_zero_when_not_late() {

        LocalDate endDate = LocalDate.now().plusDays(5);

        long result = returnBorrowPolicy.calculateDaysLate(endDate, LocalDate.now());

        assertThat(result).isZero();
    }

    @Test
    void calculateDaysLate_should_return_positive_value_when_late() {

        LocalDate endDate = LocalDate.now().minusDays(10);

        long result = returnBorrowPolicy.calculateDaysLate(endDate, LocalDate.now());

        assertThat(result).isPositive();
    }

    @Test
    void calculateFine_should_return_zero_when_no_late_days() {

        BigDecimal result = returnBorrowPolicy.calculateFine(0L, BigDecimal.valueOf(100));

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calculateFine_should_calculate_expected_amount() {

        BigDecimal result = returnBorrowPolicy.calculateFine(5L, BigDecimal.valueOf(100));

        assertThat(result).isEqualByComparingTo("500");
    }

    @Test
    void isLate_should_return_false_when_days_late_is_zero() {

        assertFalse(returnBorrowPolicy.isLate(0));
    }

    @Test
    void isLate_should_return_false_when_days_late_is_negative() {

        assertFalse(returnBorrowPolicy.isLate(-1));
    }

    @Test
    void isLate_should_return_true_when_days_late_is_positive() {

        assertTrue(returnBorrowPolicy.isLate(1));
    }
}