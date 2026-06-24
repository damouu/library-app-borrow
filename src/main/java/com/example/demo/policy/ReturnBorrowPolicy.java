package com.example.demo.policy;

import com.example.demo.exception.UnreturnedBorrowExistsException;
import com.example.demo.model.Borrow;
import com.example.demo.util.DateCalculationUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Component
public class ReturnBorrowPolicy {

    public Borrow validateAndGetBorrow(List<Borrow> borrows) {
        if (borrows.isEmpty()) {
            throw new UnreturnedBorrowExistsException("まだ貸出返却されていないの貸し出しがあります。");
        }
        return borrows.getFirst();
    }

    public long calculateDaysLate(LocalDate endDate, LocalDate currentDate) {
        return DateCalculationUtil.calculateWorkingDays(endDate.plusDays(1), currentDate);
    }

    public BigDecimal calculateFine(long daysLate, BigDecimal rate) {
        return BigDecimal.valueOf(daysLate).multiply(rate).setScale(0, RoundingMode.HALF_UP);
    }

    public boolean isLate(long daysLate) {
        return daysLate > 0;
    }
}