//package com.example.demo.integration.controller;
//
//import com.example.demo.model.Book;
//import com.example.demo.model.Series;
//import com.example.demo.repository.BookRepository;
//import com.example.demo.repository.SeriesRepository;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.boot.web.server.LocalServerPort;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//
//import javax.transaction.Transactional;
//import java.net.URI;
//import java.time.LocalDate;
//import java.util.HashMap;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.UUID;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//public class BookIntegrationTest {
//
//    @LocalServerPort
//    private int port;
//
//    @Autowired
//    private TestRestTemplate restTemplate;
//
//    @Autowired
//    private BookRepository bookRepository;
//
//    @MockBean
//    private SeriesRepository seriesRepository;
//
//
//    @Test
//    @Transactional
//    void getBookUuid() {
//        Series series = new Series();
//        seriesRepository.save(series);
//        ResponseEntity<Series> responseEntity = restTemplate.getForEntity("http://localhost:" + port + "/api/public/series/" + series.getSeriesUUID(), Series.class);
//        Assertions.assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
//        Assertions.assertTrue(Objects.requireNonNull(responseEntity.getHeaders().getContentType()).isCompatibleWith(MediaType.APPLICATION_JSON));
////        Assertions.assertEquals(Objects.requireNonNull(responseEntity.getBody()).getBookUUID(), book.getBookUUID());
//    }
//
//    @Test
//    void postBook() {
//        Book book = new Book(null, "tittle", "genre", 200, "publisher", "author", LocalDate.now());
//        ResponseEntity<Book> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/book", book, Book.class);
//        Optional<Book> book1 = bookRepository.findByUuid(Objects.requireNonNull(responseEntity.getBody()).getBookUUID());
//        Assertions.assertEquals(201, responseEntity.getStatusCodeValue());
//        Assertions.assertEquals(URI.create("http://localhost:8083/api/book/" + book1.get().getBookUUID()), responseEntity.getHeaders().getLocation());
//        Assertions.assertEquals(responseEntity.getBody().getBookUUID(), book1.get().getBookUUID());
//    }
//
//    @Test
//    void deleteBook() {
//        Book book = new Book(UUID.randomUUID(), "DELETE", "DELETE", 200, "publisher", "author", LocalDate.now());
//        bookRepository.save(book);
//        restTemplate.delete("http://localhost:" + port + "/api/book/" + book.getBookUUID());
//        ResponseEntity<Book> responseEntity = restTemplate.getForEntity("http://localhost:" + port + "/api/book/" + book.getBookUUID(), Book.class);
//        Assertions.assertEquals(404, responseEntity.getStatusCodeValue());
//        Assertions.assertNotEquals(book.getBookUUID(), responseEntity.getBody().getBookUUID());
//    }
//
//    @Test
//    void updateBook() {
//        Book book = new Book(UUID.randomUUID(), "title", "genre", 200, "publisher", "author", LocalDate.now());
//        bookRepository.save(book);
//        HashMap<String, String> bookUpdates = new HashMap<>();
//        bookUpdates.put("title", "UPDATED");
//        bookUpdates.put("genre", "UPDATED");
//        restTemplate.put("http://localhost:" + port + "/api/book/" + book.getBookUUID(), bookUpdates);
//        ResponseEntity<Book> responseEntity = restTemplate.getForEntity("http://localhost:" + port + "/api/book/" + book.getBookUUID(), Book.class);
//        Assertions.assertEquals(200, responseEntity.getStatusCodeValue());
//        Assertions.assertEquals(book.getBookUUID(), responseEntity.getBody().getBookUUID());
////        Assertions.assertEquals(bookUpdates.get("title"), Objects.requireNonNull(responseEntity.getBody()).getTitle());
////        Assertions.assertEquals(bookUpdates.get("genre"), Objects.requireNonNull(responseEntity.getBody()).getGenre());
////        Assertions.assertNotEquals(book.getTitle(), Objects.requireNonNull(responseEntity.getBody()).getTitle());
////        Assertions.assertNotEquals(book.getGenre(), Objects.requireNonNull(responseEntity.getBody()).getGenre());
//    }
//
//}
