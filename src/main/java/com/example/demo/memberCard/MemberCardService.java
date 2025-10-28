package com.example.demo.memberCard;

import com.example.demo.book.Book;
import com.example.demo.book.BookMemberCard;
import com.example.demo.book.BookRepository;
import com.example.demo.book.BookStudentRepository;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

/**
 * The type Member card service.
 */
@Service
@Data
public class MemberCardService {

    private final BookRepository bookRepository;

    private final MemberCardRepository memberCardRepository;

    private final BookStudentRepository bookMemberCardRepository;


    public ResponseEntity<List<MemberCard>> getMemberCards(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MemberCard> pages = memberCardRepository.findAll(pageable);
        return ResponseEntity.ok(pages.toList());
    }

    public ResponseEntity<LinkedHashMap<String, Object>> getMemberCard(UUID memberCardUUID) throws ResponseStatusException {
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        Optional<MemberCard> MemberCard = Optional.ofNullable(memberCardRepository.findMemberCardByUuid(memberCardUUID).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "student card does not exist")));
//        Set<Book> book = Collections.singleton(MemberCard.get().getBooks().get(0));
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> list = new LinkedHashMap<>();
//        list.put("book", book.size());
        response.put("status", "success");
        response.put("type", "Collection");
        response.put("size", list);
//        data.put("book", book);
        response.put("data", data);
        return ResponseEntity.status(200).body(response);
    }

    public ResponseEntity deleteMemberCard(UUID memberCardUUID) throws ResponseStatusException {
        Optional<MemberCard> memberCard = Optional.ofNullable(memberCardRepository.findMemberCardByUuid(memberCardUUID).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "student card does not exist")));
        memberCard.get().setDeleted_at(LocalDateTime.now());
        memberCardRepository.save(memberCard.get());
        return ResponseEntity.status(204).build();
    }

    public ResponseEntity<MemberCard> postMemberCard(UUID memberCardUUID) throws ResponseStatusException {
        MemberCard memberCard = new MemberCard(memberCardUUID);
        memberCard.setCreated_at(LocalDateTime.now());
        memberCard.setValid_until(LocalDateTime.now().plusYears(2));
        memberCardRepository.save(memberCard);
        return ResponseEntity.status(HttpStatus.CREATED).location(URI.create("http://localhost:8083/api/studentCard/" + memberCard.getMember_card_uuid())).body(memberCard);
    }

    public ResponseEntity<HashMap<String, Object>> borrowBooks(UUID memberCardUUID, Map<Object, ArrayList<UUID>> booksArrayJson) throws ResponseStatusException {
        MemberCard memberCard = memberCardRepository.findMemberCardByUuid(memberCardUUID).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "member card does not exist"));
        UUID uuid = UUID.randomUUID();
        for (UUID bookUUID : booksArrayJson.get("books_UUID")) {
            if (!bookMemberCardRepository.existsByBookIsBorrowed(bookUUID)) {
                BookMemberCard bookMemberCard = new BookMemberCard();
                bookMemberCard.setBorrow_start_date(LocalDate.now());
                bookMemberCard.setBorrow_end_date(LocalDate.now().plusWeeks(2));
                bookMemberCard.setBorrow_uuid(uuid);
                bookMemberCard.setMemberCard(memberCard);
                Book book = bookRepository.findByUuid(bookUUID).get();
                book.set_borrowed(true);
                bookMemberCard.setBook(book);
                bookRepository.save(book);
                bookMemberCardRepository.save(bookMemberCard);
            } else {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "error");
            }
        }

        HashMap<String, Object> response = new LinkedHashMap<>();
        HashMap<String, String> data = new LinkedHashMap<>();
        response.put("success", true);
        response.put("status", 201);
        response.put("message", booksArrayJson.get("books_UUID").size() + "冊の本は貸し出しされる完了です。");
        response.put("data", data);
        data.put("borrow_UUID", uuid.toString());
        data.put("start_borrow_date", String.valueOf(LocalDate.now()));
        data.put("return_borrow_date", String.valueOf(LocalDate.now().plusWeeks(2)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
        Optional<List<BookMemberCard>> bookMemberCard = Optional.ofNullable(bookMemberCardRepository.findBookStudentByBorrow_uuid(borrowUUID)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "borrow does not exist"));
        // 受信されたの会員番号カードと貸出の番号を合ってるなら進歩する反面合ってない状況ならエラーの外例を発生されます。。
        if (!Objects.equals(bookMemberCard.get().get(0).getMemberCard().getMember_card_uuid().toString(), memberCardUUID.toString())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "error");
        }
        boolean return_date_is_late = false;
        String message = null;
        HashMap<String, Object> response = new LinkedHashMap<>();
        HashMap<String, String> data = new LinkedHashMap<>();
        response.put("success", true);
        response.put("status", 200);
        // 予定されたの返すの日程を超えちゃうなら何日で数えて罰金を判断されて科します。一日の遅らすに従って五百円の金額が定められたです。
        if (bookMemberCard.get().get(0).getBorrow_end_date().isBefore(LocalDate.now())) {
            return_date_is_late = true;
            final long days = ChronoUnit.DAYS.between(bookMemberCard.get().get(0).getBorrow_end_date(), LocalDate.now());
            message = "貸し出しされたの本は" + days + "日で遅刻を返却されましたので" + days * 500 + "円の罰金を科します。";
            response.put("message", message);
        }
        for (BookMemberCard optionalBookMemberCard : bookMemberCard.get()) {
            optionalBookMemberCard.setBorrow_return_date(LocalDate.now());
            optionalBookMemberCard.getBook().set_borrowed(false);
            bookRepository.save(optionalBookMemberCard.getBook());
            bookMemberCardRepository.save(optionalBookMemberCard);
        }
        response.put("return_lately", return_date_is_late);
        response.put("data", data);
        data.put("start_borrow_date", String.valueOf(bookMemberCard.get().get(0).getBorrow_return_date()));
        data.put("expected_return_borrow_date", String.valueOf(bookMemberCard.get().get(0).getBorrow_end_date()));
        data.put("actual_return_borrow_date", String.valueOf(bookMemberCard.get().get(0).getBorrow_start_date()));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
