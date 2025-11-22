package com.example.demo.service;

import com.example.demo.model.Book;
import com.example.demo.model.BookMemberCard;
import com.example.demo.model.MemberCard;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.BookStudentRepository;
import com.example.demo.repository.MemberCardRepository;
import com.example.demo.repository.SeriesRepository;
import com.example.demo.spec.BookSpecification;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.LocalDate;
import java.util.*;

/**
 * The type Book service.
 */
@Service
@Data
public class BookService {

    private final BookRepository bookRepository;

    private final MemberCardRepository memberCardRepository;
    private final SeriesRepository seriesRepository;
    private final BookStudentRepository bookStudentRepository;

    /**
     * Gets books.
     *
     * @param allParams the all params
     * @return the books
     */
    public ResponseEntity<?> getBooks(Map allParams) {
        PageRequest pageable;
        if (allParams.size() == 2 && allParams.containsKey("page") && allParams.containsKey("size")) {
            pageable = PageRequest.of(Integer.parseInt((String) allParams.get("page")), Integer.parseInt((String) allParams.get("size")));
            Page<Book> pages = bookRepository.findAll(pageable);
            return ResponseEntity.ok(pages.toList());
        } else {
            final Specification<Book> specification = BookSpecification.filterBook(allParams);
            if (!allParams.containsKey("page") && !allParams.containsKey("size")) {
                pageable = PageRequest.of(1, 20, Sort.unsorted());
            } else if (allParams.size() > 2 && allParams.containsKey("page") && allParams.containsKey("size")) {
                pageable = PageRequest.of(Integer.parseInt((String) allParams.get("page")), Integer.parseInt((String) allParams.get("size")), Sort.unsorted());
            } else {
                if (allParams.containsKey("page")) {
                    pageable = PageRequest.of(Integer.parseInt((String) allParams.get("page")), 20);
                } else {
                    pageable = PageRequest.of(1, Integer.parseInt((String) allParams.get("size")));
                }
            }
            final Page<Book> books = (bookRepository.findAll(specification, pageable));
            return ResponseEntity.ok(books);
        }
    }

    /**
     * Search books list.
     *
     * @param search the search
     * @return the list
     * @throws ResponseStatusException the response status exception
     */
    public List<Book> searchBooks(String search) throws ResponseStatusException {
        final Specification<Book> specification = BookSpecification.searchBook(search);
        final List<Book> books = bookRepository.findAll(specification);
        if (books.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return books;
    }

    /**
     * find a book by the passed UUID.
     *
     * @param bookUuid a book's UUID
     * @return Array returns if the given UUID exists, a book resource and also attach to it, the studentCard associated to it.
     * @throws ResponseStatusException throws an exception if the given UUID does not correspond to a book in the database.
     */
    public ResponseEntity<Map<String, Map<String, String>>> getBookUuid(UUID bookUuid) throws ResponseStatusException {
        Book book = bookRepository.findByUuid(bookUuid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "book not found"));
        Map<String, String> bookData = new HashMap<>();
        Map<String, Map<String, String>> response = new HashMap<>();
//        if (book.getMemberCard() != null) {
//            Map<String, String> studentCard = new HashMap<>();
//            studentCard.put("memberCardUUID", String.valueOf(book.getMemberCard().getBookUUID()));
//            response.put("studentCard", studentCard);
//        }
//        bookData.put("UUID", String.valueOf(book.getBookUUID()));
//        bookData.put("author", book.getAuthor());
//        bookData.put("genre", book.getGenre());
//        bookData.put("publisher", book.getPublisher());
//        bookData.put("title", book.getTitle());
//        bookData.put("total_pages", String.valueOf(book.getTotalPages()));
//        bookData.put("created_at", String.valueOf(book.getCreated_at()));
        response.put("book", bookData);
        return ResponseEntity.ok(response);
    }

    /**
     * deletes a book by providing a book's UUID, if the given UUID does not correspond to an existing bok in the db, throws an error
     *
     * @param bookUuid a book's UUID
     * @return returns an ResponseEntity object with a confirmation message in it, confirming the book is deleted.
     * @throws ResponseStatusException throws if the given UUID does not correspond to an existing book in the db.
     */
    public ResponseEntity<?> deleteBook(UUID bookUuid) throws ResponseStatusException {
        Book book = bookRepository.findByUuid(bookUuid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "book not found"));
        book.setDeleted_at(LocalDate.now());
        bookRepository.save(book);
        return ResponseEntity.accepted().build();
    }

    /**
     * inserts the passed book object into the database.
     *
     * @param book a book's UUID
     * @return ResponseEntity return with a location URI in the header of the response with the created book in it.
     * @throws ResponseStatusException throws if the given UUID does not correspond to an existing book in the db.
     */
    public ResponseEntity<Book> postBook(Book book) {
        book.setBookUUID(UUID.randomUUID());
        bookRepository.save(book);
        return ResponseEntity.status(201).location(URI.create("http://localhost:8083/api/book/" + book.getBookUUID())).body(book);
    }

    /**
     * Update book response entity.
     *
     * @param bookUuid    the wanted book to be updated.
     * @param bookUpdates a key value array containing the updated data to be replaced with the current book.
     * @return ResponseEntity returns a ResponseEntity object with a URI location of the updated book.
     * @throws ResponseStatusException throws an exception if the given UUID does not exist in the database.
     */
    public ResponseEntity<Book> updateBook(UUID bookUuid, HashMap<String, String> bookUpdates) {
        Book book = bookRepository.findByUuid(bookUuid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "book not found"));
        ObjectMapper oMapper = new ObjectMapper();
        Map map = oMapper.convertValue(book, Map.class);
        for (Object s : map.keySet()) {
            for (String s1 : bookUpdates.keySet()) {
                if (s.equals(s1)) {
                    map.put(s, bookUpdates.get(s1));
                }
            }
        }
        Book book1 = oMapper.convertValue(map, Book.class);
        book1.setId(book.getId());
        bookRepository.save(book1);
        Book book11 = bookRepository.findByUuid(book1.getBookUUID()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "book not found"));
        return ResponseEntity.status(201).location(URI.create("http://localhost:8083/api/book/" + book11.getBookUUID())).body(book11);
    }


    /**
     * Insert student borrow books response entity.
     *
     * @param bookUuid           the book uuid
     * @param memberCardUUIDCard the student uuid card
     * @return {@code } the response entity
     */
    //fixme add to the BookStudent entity a borrow_end_date property value when saved into the db
    //todo create a youtrack ticket for the change to be associated to a ticket
    //todo add a borrow_return_date of two weeks when borrowing a book.
    public ResponseEntity<?> insertStudentBorrowBooks(UUID bookUuid, UUID memberCardUUIDCard) {
        Book book = bookRepository.findByUuid(bookUuid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "book does not exist"));
        MemberCard MemberCard = memberCardRepository.findMemberCardByUuid(memberCardUUIDCard).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "MemberCard does not exist" + memberCardUUIDCard));
        Optional<BookMemberCard> bookStudent = bookStudentRepository.findBookStudentByID(book.getId());
        int status;
        String response;
        if (bookStudent.isPresent() && bookStudent.get().getBorrowReturnDate() == null) {
            status = 422;
            response = "already borrowed";
        } else {
            BookMemberCard BookMemberCard1 = new BookMemberCard(LocalDate.now(), LocalDate.now().plusWeeks(2), null);
            BookMemberCard1.setBook(book);
            BookMemberCard1.setMemberCard(MemberCard);
            long date = System.currentTimeMillis() + 14 * 24 * 3600 * 1000;
            Date newDate = new Date(date);
//            bookStudent.get().setBorrow_end_date(newDate);
            bookStudentRepository.save(BookMemberCard1);
            status = 201;
            response = "created";
        }
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Return student borrow books response entity.
     *
     * @param bookUuid           the book uuid
     * @param memberCardUUIDCard the student uuid card
     * @return the response entity
     */
    public ResponseEntity<?> returnStudentBorrowBooks(UUID bookUuid, UUID memberCardUUIDCard) {
        Book book = bookRepository.findByUuid(bookUuid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "book does not exist"));
        Optional<BookMemberCard> bookStudent = bookStudentRepository.findBookStudentByID(book.getId());
        int status;
        String response;
        if (bookStudent.isPresent() && bookStudent.get().getBorrowReturnDate() == null) {
            Date borrow_request_date = new Date();
            bookStudent.get().setBorrowReturnDate(LocalDate.now());
            bookStudentRepository.save(bookStudent.get());
            status = 200;
            response = "book returned";
        } else {
            status = 400;
            response = "error";
        }
        return ResponseEntity.status(status).body(response);
    }


    /**
     * Return student borrow books response entity.
     *
     * @param bookUuid           the book uuid
     * @param memberCardUUIDCard the student uuid card
     * @return the response entity
     */
    public ResponseEntity<?> updateReturnStudentBorrowBook(UUID bookUuid, UUID memberCardUUIDCard) {
        Book book = bookRepository.findByUuid(bookUuid).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "book does not exist"));
        MemberCard MemberCard = memberCardRepository.findMemberCardByUuid(memberCardUUIDCard).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "MemberCard does not exist" + memberCardUUIDCard));
        Optional<BookMemberCard> bookStudent = bookStudentRepository.findBookStudentByIDUpdate(book.getId());
        int status;
        String response;
        if (bookStudent.isPresent() && bookStudent.get().getBorrowReturnDate() == null) {
            long date = System.currentTimeMillis() + 14 * 24 * 3600 * 1000;
            Date newDate = new Date(date);
//            bookStudent.get().setGranted_borrow_extend(true);
            bookStudent.get().setBorrowEndDate(LocalDate.now());
            bookStudentRepository.save(bookStudent.get());
            status = 201;
            response = "updated";
        } else {
            status = 401;
            response = "error";
        }
        return ResponseEntity.status(status).body(response);
    }

//    /**
//     * Gets books student card.
//     *
//     * @param memberCardUUIDCard the student uuid card
//     * @return the books student card
//     */
//    public ResponseEntity<?> getBooksStudentCard(UUID memberCardUUIDCard) {
//        MemberCard MemberCard = memberCardRepository.findMemberCardByUuid(memberCardUUIDCard).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "MemberCard does not exist" + memberCardUUIDCard));
//        if (!MemberCard.getBooks().isEmpty()) {
//            return ResponseEntity.status(200).body(MemberCard.getBooks());
//        }
//        return ResponseEntity.noContent().build();
//    }
}
