package com.example.demo.service;

import com.example.demo.model.Book;
import com.example.demo.model.BookMemberCard;
import com.example.demo.model.MemberCard;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.BookStudentRepository;
import com.example.demo.repository.BorrowSummaryRepository;
import com.example.demo.repository.MemberCardRepository;
import com.example.demo.util.PaginationUtil;
import com.example.demo.view.BorrowSummaryView;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Data
public class MemberCardService {

    private final BookRepository bookRepository;

    private final MemberCardRepository memberCardRepository;

    private final BookStudentRepository bookMemberCardRepository;

    private final BorrowSummaryRepository borrowSummaryRepository;


    public ResponseEntity deleteMemberCard(UUID memberCardUUID) throws ResponseStatusException {
        Optional<MemberCard> memberCard = Optional.ofNullable(memberCardRepository.findMemberCardByUuid(memberCardUUID).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "member card does not exist")));
        memberCard.get().setDeleted_at(LocalDateTime.now());
        memberCardRepository.save(memberCard.get());
        return ResponseEntity.status(204).build();
    }

    public ResponseEntity<MemberCard> postMemberCard(UUID memberCardUUID) throws ResponseStatusException {
        MemberCard memberCard = new MemberCard(memberCardUUID);
        memberCard.setCreatedAT(LocalDateTime.now());
        memberCard.setValidUntil(LocalDateTime.now().plusYears(2));
        memberCardRepository.save(memberCard);
        return ResponseEntity.status(HttpStatus.CREATED).location(URI.create("http://localhost:8083/api/studentCard/" + memberCard.getMemberCardUUID())).body(memberCard);
    }

    public ResponseEntity<HashMap<String, Object>> borrowBooks(UUID memberCardUUID, Map<Object, ArrayList<UUID>> booksArrayJson) throws ResponseStatusException {
        MemberCard memberCard = memberCardRepository.findMemberCardByUuid(memberCardUUID).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "member card does not exist"));
        if (bookMemberCardRepository.getBookMemberCardByBook(memberCard)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        } else {
            UUID uuid = UUID.randomUUID();
            for (UUID bookUUID : booksArrayJson.get("books_UUID")) {
                if (!bookMemberCardRepository.existsByBookIsBorrowed(bookUUID)) {
                    BookMemberCard bookMemberCard = new BookMemberCard();
                    bookMemberCard.setBorrowStartDate(LocalDate.now());
                    bookMemberCard.setBorrowEndDate(LocalDate.now().plusWeeks(2));
                    bookMemberCard.setBorrowUUID(uuid);
                    bookMemberCard.setMemberCard(memberCard);
                    Book book = bookRepository.findByUuid(bookUUID).get();
                    book.setCurrentlyBorrowed(true);
                    bookMemberCard.setBook(book);
                    bookRepository.save(book);
                    bookMemberCardRepository.save(bookMemberCard);
                } else {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "error");
                }
            }
            HashMap<String, Object> response = new LinkedHashMap<>();
            HashMap<String, String> data = new LinkedHashMap<>();
            response.put("message", booksArrayJson.get("books_UUID").size() + "冊の本は貸し出しされる完了です。");
            response.put("data", data);
            data.put("borrow_UUID", uuid.toString());
            data.put("start_borrow_date", String.valueOf(LocalDate.now()));
            data.put("return_borrow_date", String.valueOf(LocalDate.now().plusWeeks(2)));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
    }


    /**
     * 貸し出しの本を返却の機能性です。
     *
     * @param memberCardUUID the member card uuid
     * @param borrowUUID     the borrow uuid
     * @return the response entity
     * @throws ResponseStatusException the response status exception
     * @implNote {@link #SD-234  https://damou.myjetbrains.com/youtrack/issue/SD-234/6LK444GX5Ye644GX5pys44KS6LU5Y20}
     */
    public ResponseEntity<HashMap<String, Object>> returnBorrowBooks(UUID memberCardUUID, UUID borrowUUID) throws ResponseStatusException {
        MemberCard memberCard = memberCardRepository.findMemberCardByUuid(memberCardUUID).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "member card does not exist"));
        Optional<List<BookMemberCard>> bookMemberCard = Optional.ofNullable(bookMemberCardRepository.findBookStudentByBorrow_uuid(borrowUUID)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "borrow does not exist"));
        // 受信されたの会員番号カードと貸出の番号を合ってるなら進歩する反面合ってない状況ならエラーの外例を発生されます。。
        if (!Objects.equals(bookMemberCard.get().get(0).getMemberCard().getMemberCardUUID().toString(), memberCardUUID.toString())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error");
        }
        boolean return_date_is_late = false;
        String message = null;
        HashMap<String, Object> response = new LinkedHashMap<>();
        HashMap<String, String> data = new LinkedHashMap<>();
        response.put("success", true);
        response.put("status", 200);
        // 予定されたの返すの日程を超えちゃうなら何日で数えて罰金を判断されて科します。一日の遅らすに従って五百円の金額が定められたです。
        if (bookMemberCard.get().get(0).getBorrowEndDate().isBefore(LocalDate.now())) {
            return_date_is_late = true;
            final long days = ChronoUnit.DAYS.between(bookMemberCard.get().get(0).getBorrowEndDate(), LocalDate.now());
            message = "貸し出しされたの本は" + days + "日で遅刻を返却されましたので" + days * 500 + "円の罰金を科します。";
            response.put("message", message);
        }
        for (BookMemberCard optionalBookMemberCard : bookMemberCard.get()) {
            optionalBookMemberCard.setBorrowReturnDate(LocalDate.now());
            optionalBookMemberCard.getBook().setCurrentlyBorrowed(false);
            bookRepository.save(optionalBookMemberCard.getBook());
            bookMemberCardRepository.save(optionalBookMemberCard);
        }
        response.put("return_lately", return_date_is_late);
        response.put("data", data);
        data.put("start_borrow_date", String.valueOf(bookMemberCard.get().get(0).getBorrowReturnDate()));
        data.put("expected_return_borrow_date", String.valueOf(bookMemberCard.get().get(0).getBorrowEndDate()));
        data.put("actual_return_borrow_date", String.valueOf(bookMemberCard.get().get(0).getBorrowStartDate()));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    /**
     * 会員のメンバ番号で貸し出しの履歴の機能性です。
     *
     * @param memberCardUUID the member card uuid
     * @return {@link ResponseEntity>} the history　of previous borrows
     * @throws ResponseStatusException the response status exception
     * @apiNote {@link #SD-233  https://damou.myjetbrains.com/youtrack/issue/SD-233/44Om44O844K244O844Gu5pys44Gu6LK444GX5Ye644GX5bGl5q20 }
     */
    public ResponseEntity<HashMap<String, Object>> getHistory(UUID memberCardUUID, Map allParams) throws ResponseStatusException {
        Pageable pageable = PaginationUtil.extractPage(allParams);
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
                books.addAll(borrowSummaryView.getBooks());
                dede.put("borrow_start_date", String.valueOf(borrowSummaryView.getBorrowStartDate()));
                dede.put("borrow_expected_end_date", String.valueOf(borrowSummaryView.getBorrowEndDate()));
                dede.put("borrow_return_date", String.valueOf(borrowSummaryView.getBorrowReturnDate()));
                if (borrowSummaryView.getBorrowReturnDate() != null && borrowSummaryView.getBorrowEndDate().isBefore(borrowSummaryView.getBorrowReturnDate())) {
                    final long days = ChronoUnit.DAYS.between(borrowSummaryView.getBorrowEndDate(), borrowSummaryView.getBorrowReturnDate());
                    return_date_is_late = true;
                    dede.put("return_date_is_late", return_date_is_late);
                    dede.put("return_date_days", days);
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
