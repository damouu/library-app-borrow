package com.example.demo.mapper;

import com.example.demo.dto.BorrowAggregate;
import com.example.demo.dto.LoanItemDetails;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
public class BorrowAssembler {

    public BorrowAggregate toAggregate(
            UUID borrowUuid,
            UUID memberCardUUID,
            LocalDate startDate,
            LocalDate endDate,
            List<LoanItemDetails> refs
    ) {
        return new BorrowAggregate(
                borrowUuid,
                memberCardUUID,
                startDate,
                endDate,
                refs
        );
    }
}