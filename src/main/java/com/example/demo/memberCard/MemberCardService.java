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
import java.util.*;

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


}
