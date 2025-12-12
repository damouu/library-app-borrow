package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.model.Borrow;
import com.example.demo.repository.BorrowRepository;
import com.example.demo.repository.BorrowSummaryRepository;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Data
@Service
@RequiredArgsConstructor
public class BorrowService {

    private final BorrowRepository borrowRepository;

    private final BorrowSummaryRepository borrowSummaryRepository;

    private final KafkaTemplate<UUID, Object> KafkaTemplate;

    /**
     * 貸し出しの本を返却の機能性です。
     * 予定されたの返すの日程を超えちゃうなら何日で数えて罰金を判断されて科します。一日の遅らすに従って五百円の金額が定められたです。
     *
     * @param memberCardUUID the member card uuid
     * @return the response entity
     * @throws ResponseStatusException the response status exception
     * @implNote {@link #SD-234  https://damou.myjetbrains.com/youtrack/issue/SD-234/6LK444GX5Ye644GX5pys44KS6LU5Y20}
     */
    @Transactional
    public ResponseEntity<Map<String, Object>> borrowBooks(UUID memberCardUUID, BookPayload booksArrayJson) throws ResponseStatusException {
        if (borrowRepository.getBookMemberCardByBook(memberCardUUID)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "まだ貸出返却されていないの貸し出しがあります。");
        }
        if (borrowRepository.existsByMemberCardUuid(memberCardUUID)) {
            boolean checkLatestBorrowDate = borrowRepository.checkLatestBorrowDate(memberCardUUID);
            if (checkLatestBorrowDate) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "一日の借入限度額に達しました。");
            }
        }
        UUID borrowUid = UUID.randomUUID();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusWeeks(2);
        List<UUID> booksUUID = booksArrayJson.getData().stream().map(LoanItemDetails::getBook_uuid).toList();
        List<BookToDecrement> booksToDecrement = booksUUID.stream().map(BookToDecrement::new).toList(); // Assuming BookToDecrement has a constructor/builder
        InventoryDataEvent inventoryData = InventoryDataEvent.builder().books(booksToDecrement).build();
        List<ChapterDetails> chapters = booksArrayJson.getData().stream().map(LoanItemDetails::getChapter).toList();
        NotificationDataEvent notificationData = NotificationDataEvent.builder().borrow_uuid(borrowUid).borrow_start_date(startDate.toString()).borrow_end_date(endDate.toString()).chapters(chapters).build();
        EventData dataPayload = EventData.builder().notificationData(notificationData).inventoryData(inventoryData).build();
        Metadata metadataPayload = Metadata.builder().event_uuid(borrowUid).event_type("LIBRARY_BORROWED").timestamp(LocalDate.now().toString()).source_service("library-app-borrow-v1").memberCardUUID(memberCardUUID).build();
        BorrowEventPayload finalPayload = BorrowEventPayload.builder().metadata(metadataPayload).data(dataPayload).build();
        List<Borrow> borrows = booksArrayJson.getData().stream().map(details -> Borrow.builder().borrowStartDate(startDate).borrowEndDate(endDate).borrowUuid(borrowUid).memberCardUuid(memberCardUUID).bookUuid(details.getBook_uuid()).chapterUUID(details.getChapter().getChapterUUID()).build()).toList();
        borrowRepository.saveAll(borrows);
        KafkaTemplate.send("library.borrow.v1", borrowUid, finalPayload);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", booksArrayJson.getData().size() + "冊の本は貸し出しされる完了です。", "data", Map.of("borrow_UUID", borrowUid.toString(), "start_borrow_date", String.valueOf(startDate), "end_borrow_date", String.valueOf(endDate))));
    }

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
        long daysLate = borrows.stream().mapToLong(borrow -> {
            if (borrow.getBorrowEndDate().isBefore(currentDate)) {
                return ChronoUnit.DAYS.between(borrow.getBorrowEndDate(), currentDate);
            }
            return 0L;
        }).max().orElse(0L);
        boolean isLate = daysLate > 0;
        long fineAmount = daysLate * 500;
        borrowRepository.setReturnDateForBorrows(borrows, currentDate);
        KafkaTemplate.send("library.return.v1", borrowUUID, booksArrayJson);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("data", Map.of("borrow_UUID", borrowUUID, "return_lately", isLate, "daysLate", daysLate, "罰金", fineAmount + "円の罰金となります。")));
    }


    // * 会員のメンバ番号で貸し出しの履歴の機能性です。
// *
// * @param memberCardUUID the member card uuid
// * @return {@link ResponseEntity>} the history　of previous borrows
// * @throws ResponseStatusException the response status exception
// * @apiNote {@link #SD-233  https://damou.myjetbrains.com/youtrack/issue/SD-233/44Om44O844K244O844Gu5pys44Gu6LK444GX5Ye644GX5bGl5q20 }
// */
    public ResponseEntity<HashMap<String, Object>> getHistory(UUID memberCardUUID, Map allParams) throws ResponseStatusException {
        Pageable pageable2 = PaginationUtil.extractPage(allParams);
        int page = pageable2.getPageNumber();
        int size = pageable2.getPageSize();
        String sortProperty = (String) allParams.get("sort");
        String sortDirection = (String) allParams.get("direction");
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortProperty));
        //　パジネーションのクエリーを実施です。
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
                    final long days = ChronoUnit.DAYS.between(borrowSummaryView.getBorrowEndDate(), borrowSummaryView.getBorrowReturnDate());
                    return_date_is_late = true;
                    dede.put("return_date_is_late", return_date_is_late);
                    dede.put("return_late_days", days);
                    dede.put("payment_fee", days * 500);
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
