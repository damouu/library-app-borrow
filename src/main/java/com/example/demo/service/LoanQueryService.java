package com.example.demo.service;

import com.example.demo.dto.ChapterBorrowCountDTO;
import com.example.demo.repository.BorrowRepository;
import com.example.demo.repository.BorrowSummaryRepository;
import com.example.demo.util.DateCalculationUtil;
import com.example.demo.util.PaginationUtil;
import com.example.demo.view.BorrowSummaryView;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Data
@Service
@RequiredArgsConstructor
public class LoanQueryService {

    private final BorrowRepository borrowRepository;

    private final BorrowSummaryRepository borrowSummaryRepository;

    private static final BigDecimal DAILY_FINE_RATE = new BigDecimal("500");


    public ResponseEntity<?> topChapters(Map allParams, String period) {
        LocalDate startDate, end, endDate;
        Pageable pageable = PaginationUtil.extractPage(allParams);

        switch (period.toLowerCase()) {
            case "lastweek":
                LocalDate today = LocalDate.now();
                startDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1);
                end = startDate.plusDays(6);
                break;
            case "lastmonth":
                startDate = LocalDate.from(LocalDate.now().minusMonths(1).withDayOfMonth(1).atStartOfDay());
                endDate = LocalDate.now().minusMonths(1);
                end = endDate.withDayOfMonth(endDate.lengthOfMonth());
                break;
            default:
                LocalDate startOfWeek = LocalDate.now();
                startDate = startOfWeek.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                end = startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        }

        List<ChapterBorrowCountDTO> topBorrowedChapters = borrowRepository.getTopBorrowedChapters(startDate, end, pageable);
        return ResponseEntity.ok(topBorrowedChapters);
    }


    /**
     * 会員のメンバ番号で貸し出しの履歴の機能性です。
     * 　パジネーションのクエリーを実施です。
     *
     * @param memberCardUUID the member card uuid
     * @return {@link ResponseEntity>} the history　of previous borrows
     * @throws ResponseStatusException the response status exception
     * @apiNote {@link #SD-233  https://damou.myjetbrains.com/youtrack/issue/SD-233/44Om44O844K244O844Gu5pys44Gu6LK444GX5Ye644GX5bGl5q20 }
     */
    public ResponseEntity<HashMap<String, Object>> getHistory(UUID memberCardUUID, Map allParams) throws ResponseStatusException {
        Pageable pageable2 = PaginationUtil.extractPage(allParams);
        int page = pageable2.getPageNumber();
        int size = pageable2.getPageSize();
        LocalDate currentDate = LocalDate.now();
        String sortProperty = (String) allParams.get("sort");
        String sortDirection = (String) allParams.get("direction");
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortProperty));
        Page<BorrowSummaryView> borrowSummaries = borrowSummaryRepository.findBorrowSummaries(memberCardUUID, pageable);
        HashMap<String, Object> response = new LinkedHashMap<>();
        response.put("memberCard_UUID", memberCardUUID.toString());
        HashMap<String, Object> borrow_history = new LinkedHashMap<>();
        if (!borrowSummaries.isEmpty()) {
            if (borrowSummaries.getContent().get(0).getBorrowReturnDate() == null) {
                response.put("unreturned_borrows", true);
                response.put("unreturned_borrow_position", 0);
            }
            for (BorrowSummaryView borrowSummaryView : borrowSummaries) {
                List<Object> books = new ArrayList<>();
                boolean return_date_is_late = false;
                HashMap<String, Object> dede = new LinkedHashMap<>();
                books.addAll(borrowSummaryView.getBookDetails());
                dede.put("borrow_start_date", String.valueOf(borrowSummaryView.getBorrowStartDate()));
                dede.put("borrow_expected_end_date", String.valueOf(borrowSummaryView.getBorrowEndDate()));
                dede.put("borrow_return_date", String.valueOf(borrowSummaryView.getBorrowReturnDate()));
                if (borrowSummaryView.getBorrowReturnDate() != null && borrowSummaryView.getBorrowEndDate().isBefore(borrowSummaryView.getBorrowReturnDate())) {
                    long daysLate = DateCalculationUtil.calculateWorkingDays(borrowSummaryView.getBorrowEndDate().plusDays(1), currentDate);
                    boolean isLate = daysLate > 0;
                    BigDecimal days = new BigDecimal(daysLate);
                    BigDecimal totalFee = days.multiply(DAILY_FINE_RATE);
                    BigDecimal fineAmount = BigDecimal.valueOf(totalFee.intValueExact());
                    return_date_is_late = isLate;
                    dede.put("return_date_is_late", return_date_is_late);
                    dede.put("return_late_days", days);
                    dede.put("payment_fee", fineAmount);
                }
                dede.put("return_date_is_late", return_date_is_late);
                dede.put("Books", books);
                borrow_history.put(String.valueOf(borrowSummaryView.getBorrowUuid()), dede);
            }
        }
        response.put("borrows_UUID", borrow_history);
        response.put("pageable", borrowSummaries.getPageable());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
