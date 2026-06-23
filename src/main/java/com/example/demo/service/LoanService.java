package com.example.demo.service;

import com.example.demo.dto.BookPayload;
import com.example.demo.dto.BorrowAggregate;
import com.example.demo.dto.BorrowCreatedSummaryDTO;
import com.example.demo.dto.ReturnCreatedEvent;
import com.example.demo.exception.DailyBorrowLimitExceededException;
import com.example.demo.exception.UnreturnedBorrowExistsException;
import com.example.demo.mapper.BorrowMapper;
import com.example.demo.model.Borrow;
import com.example.demo.repository.BorrowRepository;
import com.example.demo.util.DateCalculationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final BorrowRepository borrowRepository;

    private final BorrowMapper borrowMapper;

    private final BorrowEventPublisher borrowEventPublisher;

    private final KafkaPayloadBuilderService payloadBuilderService;

    private final KafkaTemplate<UUID, Object> kafkaTemplate;

    private static final BigDecimal DAILY_FINE_RATE = new BigDecimal("500");

    /**
     * 貸し出しの本を返却の機能性です。
     * 予定されたの返すの日程を超えちゃうなら何日で数えて罰金を判断されて科します。一日の遅らすに従って五百円の金額が定められたです。
     *
     * @param memberCardUUID the member card uuid
     * @param booksArrayJson the member card uuid
     * @return the response entity
     * @throws DailyBorrowLimitExceededException the response status exception
     * @throws UnreturnedBorrowExistsException   the response status exception
     */
    @Transactional
    public BorrowCreatedSummaryDTO borrowBooks(UUID memberCardUUID, BookPayload booksArrayJson) throws DailyBorrowLimitExceededException, UnreturnedBorrowExistsException {
        if (borrowRepository.getBookMemberCardByBook(memberCardUUID)) {
            throw new UnreturnedBorrowExistsException();
        }
        if (borrowRepository.existsByMemberCardUuid(memberCardUUID)) {
            boolean checkLatestBorrowDate = borrowRepository.checkLatestBorrowDate(memberCardUUID);
            if (checkLatestBorrowDate) {
                throw new DailyBorrowLimitExceededException();
            }
        }
        UUID borrow_uuid = UUID.randomUUID();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusWeeks(2);
        BorrowAggregate aggregate = new BorrowAggregate(borrow_uuid, memberCardUUID, startDate, endDate, booksArrayJson.data());
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
     * @throws ResponseStatusException the response status exception
     * @implNote {@link #SD-234  https://damou.myjetbrains.com/youtrack/issue/SD-234/6LK444GX5Ye644GX5pys44KS6LU5Y20}
     */
    @Transactional
    public ResponseEntity<Map<String, Map<String, Serializable>>> returnBorrowBooks(UUID memberCardUUID, UUID borrowUUID, @RequestBody BookPayload booksArrayJson) throws ResponseStatusException {
        LocalDate currentDate = LocalDate.now();
        List<Borrow> borrows = borrowRepository.getBorrowsByBorrowUuidAndMemberCardUuidAndBorrowReturnDateIsNull(borrowUUID, memberCardUUID);
        if (borrows.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", Map.of("dede", "該当する貸し出しが見つかりませんでした。")));
        }
        LocalDate borrowEndDate = borrows.getFirst().getBorrowEndDate();
        long daysLate = DateCalculationUtil.calculateWorkingDays(borrowEndDate.plusDays(1), currentDate);
        boolean isLate = daysLate > 0;
        BigDecimal days = new BigDecimal(daysLate);
        BigDecimal totalFee = days.multiply(DAILY_FINE_RATE);
        BigDecimal fineAmount = BigDecimal.valueOf(totalFee.intValueExact());
        LocalDate startDate = borrows.getFirst().getBorrowStartDate();
        LocalDate endDate = borrows.getFirst().getBorrowEndDate();
        ReturnCreatedEvent finalPayload = payloadBuilderService.buildReturnPayload(memberCardUUID, booksArrayJson, borrowUUID, "LIBRARY_RETURNED", "library-app-borrow-v1", startDate, endDate, currentDate, isLate, daysLate, fineAmount);
        borrowRepository.setReturnDateForBorrows(borrows, currentDate);
        kafkaTemplate.send("library.return.v1", borrowUUID, finalPayload);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("data", Map.of("borrow_UUID", borrowUUID, "return_lately", isLate, "days_late", daysLate, "fine_amount", fineAmount)));
    }

}