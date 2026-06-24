package com.example.demo.unit.service;

import com.example.demo.dto.LoanItemDetails;
import com.example.demo.dto.ReturnBorrowAggregate;
import com.example.demo.mapper.ReturnBorrowAssembler;
import com.example.demo.model.Borrow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReturnBorrowAssemblerTest {

    private final ReturnBorrowAssembler assembler = new ReturnBorrowAssembler();

    @Test
    @DisplayName("Should correctly assemble ReturnBorrowAggregate from inputs")
    void shouldAssembleAggregateSuccessfully() {
        Borrow mockBorrow = mock(Borrow.class);
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 14);
        when(mockBorrow.getBorrowStartDate()).thenReturn(startDate);
        when(mockBorrow.getBorrowEndDate()).thenReturn(endDate);
        UUID borrowUuid = UUID.randomUUID();
        UUID memberCardUuid = UUID.randomUUID();
        LocalDate currentDate = LocalDate.of(2026, 1, 15);
        boolean isLate = true;
        long daysLate = 1L;
        BigDecimal fineAmount = new BigDecimal("5.00");
        List<LoanItemDetails> items = List.of(new LoanItemDetails(UUID.randomUUID(), UUID.randomUUID()));
        ReturnBorrowAggregate result = assembler.toAggregate(mockBorrow, borrowUuid, memberCardUuid, currentDate, isLate, daysLate, fineAmount, items);
        assertNotNull(result);
        assertEquals(borrowUuid, result.borrowUuid());
        assertEquals(memberCardUuid, result.memberCardUuid());
        assertEquals(startDate.toString(), result.startDate());
        assertEquals(endDate.toString(), result.endDate());
        assertEquals(currentDate.toString(), result.returnDate());
        assertEquals(isLate, result.isLate());
        assertEquals(daysLate, result.daysLate());
        assertEquals(fineAmount, result.fineAmount());
        assertEquals(items, result.items());
    }
}