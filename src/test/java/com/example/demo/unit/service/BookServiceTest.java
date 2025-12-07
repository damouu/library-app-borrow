//package com.example.demo.unit.service;
//
//import org.instancio.Instancio;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.ResponseEntity;
//
//import java.net.URI;
//import java.time.LocalDate;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Objects;
//import java.util.UUID;
//
//@ExtendWith(MockitoExtension.class)
//class BookServiceTest {
//
//    @Mock
//    private BookRepository bookRepository;
//
//    @InjectMocks
//    private BookService bookService;
//
//    Book book;
//
//    @BeforeEach
//    void setUp() {
//        book = Instancio.create(Book.class);
//
//    }
//
//
//    @Test
//    void getBookUuid() {
//
//        Mockito.when(bookRepository.findByUuid(book.getBookUUID())).thenReturn(java.util.Optional.of(book));
//        ResponseEntity<Map<String, Map<String, String>>> responseEntity = bookService.getBookUuid(book.getBookUUID());
//        Assertions.assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
//        Assertions.assertNotNull(responseEntity.getBody());
//    }
//
//    @Test
//    void deleteBook() {
//        Mockito.when(bookRepository.findByUuid(book.getBookUUID())).thenReturn(java.util.Optional.of(book));
//        ResponseEntity<?> responseEntity = bookService.deleteBook(book.getBookUUID());
//        Assertions.assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
//        Assertions.assertNotNull(book.getDeleted_at());
//        Mockito.verify(bookRepository, Mockito.times(1)).save(book);
//    }
//
//    @Test
//    void postBook() {
//        ResponseEntity<Book> responseEntity = bookService.postBook(book);
//        Assertions.assertEquals(201, responseEntity.getStatusCodeValue());
//        Assertions.assertEquals(URI.create("http://localhost:8083/api/book/" + book.getBookUUID()), responseEntity.getHeaders().getLocation());
//        Assertions.assertEquals(responseEntity.getBody(), book);
//        Mockito.verify(bookRepository, Mockito.times(1)).save(book);
//    }
//
//    @Test
//    void updateBook() {
//        Book book = new Book(UUID.randomUUID(), "tittle", "genre", 200, "publisher", "author", LocalDate.now());
//        HashMap<String, String> bookUpdates = new HashMap<>();
//        bookUpdates.put("title", "UPDATED");
//        bookUpdates.put("genre", "UPDATED");
//        Book book1 = new Book(book.getBookUUID(), "UPDATED", "UPDATED", 200, "publisher", "author", LocalDate.now());
//        Mockito.when(bookRepository.findByUuid(book.getBookUUID())).thenReturn(java.util.Optional.of(book));
//        Mockito.when(bookRepository.findByUuid(book1.getBookUUID())).thenReturn(java.util.Optional.of(book1));
//        ResponseEntity<Book> responseEntity = bookService.updateBook(book.getBookUUID(), bookUpdates);
//        Assertions.assertEquals(200, responseEntity.getStatusCodeValue());
//        Mockito.verify(bookRepository, Mockito.times(2)).findByUuid(book.getBookUUID());
//        Assertions.assertEquals(Objects.requireNonNull(responseEntity.getBody()).getBookUUID(), book.getBookUUID());
//        Assertions.assertEquals(URI.create("http://localhost:8083/api/book/" + book.getBookUUID()), responseEntity.getHeaders().getLocation());
//    }
//
//}