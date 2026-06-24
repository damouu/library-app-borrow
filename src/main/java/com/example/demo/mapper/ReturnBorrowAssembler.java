package com.example.demo.mapper;

import com.example.demo.dto.LoanItemDetails;
import com.example.demo.dto.ReturnBorrowAggregate;
import com.example.demo.model.Borrow;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public class ReturnBorrowAssembler {

    public ReturnBorrowAggregate toAggregate(
            Borrow borrow,
            UUID borrowUUID,
            UUID memberCardUUID,
            LocalDate currentDate,
            boolean isLate,
            long daysLate,
            BigDecimal fineAmount,
            List<LoanItemDetails> items
    ) {
        return new ReturnBorrowAggregate(
                borrowUUID,
                memberCardUUID,
                borrow.getBorrowStartDate().toString(),
                borrow.getBorrowEndDate().toString(),
                currentDate.toString(),
                isLate,
                daysLate,
                fineAmount,
                items
        );
    }
}