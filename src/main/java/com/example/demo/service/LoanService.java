package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.event.publisher.BorrowEventPublisher;
import com.example.demo.mapper.BorrowAssembler;
import com.example.demo.mapper.BorrowMapper;
import com.example.demo.mapper.ReturnBorrowAssembler;
import com.example.demo.mapper.ReturnMapper;
import com.example.demo.model.Borrow;
import com.example.demo.policy.BorrowPolicy;
import com.example.demo.policy.ReturnBorrowPolicy;
import com.example.demo.repository.BorrowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final BorrowRepository borrowRepository;

    private final BorrowMapper borrowMapper;

    private final ReturnMapper returnMapper;

    private final ReturnBorrowPolicy returnBorrowPolicy;

    private final BorrowPolicy borrowPolicy;

    private final BorrowEventPublisher borrowEventPublisher;

    private final ReturnBorrowAssembler returnBorrowAssembler;

    private final BorrowAssembler borrowAssembler;

    private static final BigDecimal DAILY_FINE_RATE = new BigDecimal("500");

    /**
     * 貸し出しの本を返却の機能性です。
     * 予定されたの返すの日程を超えちゃうなら何日で数えて罰金を判断されて科します。一日の遅らすに従って五百円の金額が定められたです。
     *
     * @param memberCardUUID the member card uuid
     * @param booksArrayJson the member card uuid
     * @return the response entity
     */
    @Transactional
    public BorrowCreatedSummaryDTO borrowBooks(UUID memberCardUUID, BookPayload booksArrayJson) {
        borrowPolicy.validateNoActiveBorrow(borrowRepository.getBookMemberCardByBook(memberCardUUID));
        boolean exists = borrowRepository.existsByMemberCardUuid(memberCardUUID);
        Boolean limitReached = borrowRepository.checkLatestBorrowDate(memberCardUUID);
        borrowPolicy.validateDailyLimit(exists, limitReached);
        UUID borrowUuid = UUID.randomUUID();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusWeeks(2);
        BorrowAggregate aggregate = borrowAssembler.toAggregate(borrowUuid, memberCardUUID, startDate, endDate, booksArrayJson.data());
        List<Borrow> entities = borrowMapper.toEntities(aggregate);
        borrowRepository.saveAll(entities);
        borrowEventPublisher.publishBorrowCreated(aggregate, entities);
        return borrowMapper.toSummaryDTO(aggregate);
    }

    /**
     * 貸し出しの本を返却の機能性です。
     * 予定されたの返すの日程を超えちゃうなら何日で数えて罰金を判断されて科します。一日の遅らすに従って五百円の金額が定められたです。
     *
     * @param memberCardUUID the member card uuid
     * @param borrowUUID     the borrow uuid
     * @return the response entity
     */
    @Transactional
    public ReturnBorrowCreatedSummaryDTO returnBorrowBooks(UUID memberCardUUID, UUID borrowUUID, BookPayload booksArrayJson) {
        LocalDate currentDate = LocalDate.now();
        List<Borrow> borrows = borrowRepository.getBorrowsByBorrowUuidAndMemberCardUuidAndBorrowReturnDateIsNull(borrowUUID, memberCardUUID);
        Borrow borrow = returnBorrowPolicy.validateAndGetBorrow(borrows);
        long daysLate = returnBorrowPolicy.calculateDaysLate(borrow.getBorrowEndDate(), currentDate);
        boolean isLate = returnBorrowPolicy.isLate(daysLate);
        BigDecimal fineAmount = returnBorrowPolicy.calculateFine(daysLate, DAILY_FINE_RATE);
        borrowRepository.setReturnDateForBorrows(borrows, currentDate);
        List<LoanItemDetails> items = booksArrayJson.data().stream().map(b -> new LoanItemDetails(b.book_uuid(), b.chapter_uuid())).toList();
        ReturnBorrowAggregate aggregate = returnBorrowAssembler.toAggregate(borrow, borrowUUID, memberCardUUID, currentDate, isLate, daysLate, fineAmount, items);
        borrowEventPublisher.publishReturnBorrowCreated(aggregate, borrows);
        return returnMapper.toSummaryDTO(aggregate);
    }

}