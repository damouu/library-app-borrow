//package com.example.demo.book;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.net.URI;
//import java.time.LocalDate;
//import java.util.HashMap;
//import java.util.UUID;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(BookController.class)
//class BookControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private BookService bookService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Test
//    void getBooks() {
//    }
//
//    @Test
//    void getBookUuid() throws Exception {
//        Book book = new Book(UUID.randomUUID(), "tittle", "genre", 200, "publisher", "author", LocalDate.now());
//        Mockito.when(bookService.getBookUuid(book.getBookUUID())).thenReturn(ResponseEntity.ok(book));
//        mockMvc.perform(get("/api/book/" + book.getBookUUID())).andExpect(status().is2xxSuccessful()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(content().json("{\"uuid\":\"" + book.getBookUUID() + "\"," + "\"title\":\"" + book.getTitle() + "\"," + "\"genre\":\"" + book.getGenre() + "\"," + "\"totalPages\":" + book.getTotalPages() + "," + "\"publisher\":\"" + book.getPublisher() + "\"," + "\"author\":\"" + book.getAuthor() + "\"," + "\"created_at\":\"" + LocalDate.now() + "\"}"));
//    }
//
//    @Test
//    void postBook() throws Exception {
//        Book book = new Book(UUID.randomUUID(), "tittle", "genre", 200, "publisher", "author", LocalDate.now());
//        Mockito.when(bookService.postBook(book)).thenReturn(ResponseEntity.status(201).location(URI.create("http://localhost:8083/api/book/" + book.getBookUUID())).body(book));
//        mockMvc.perform(post("/api/book/").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(book))).andExpect(status().is2xxSuccessful());
//    }
//
//    @Test
//    void deleteBook() throws Exception {
//        Book book = new Book(UUID.randomUUID(), "tittle", "genre", 200, "publisher", "author", LocalDate.now());
//        Mockito.when(bookService.deleteBook(book.getBookUUID())).thenReturn(ResponseEntity.accepted().build());
//        mockMvc.perform(delete("/api/book/" + book.getBookUUID())).andExpect(status().is2xxSuccessful());
//    }
//
//    @Test
//    void updateBook() throws Exception {
//        Book book = new Book(UUID.randomUUID(), "NON-UPDATED", "NON-UPDATED", 200, "NON-UPDATED", "NON-UPDATED", LocalDate.now());
//        Book bookUpdate = new Book(book.getBookUUID(), "UPDATED", "UPDATED", 500, "UPDATED", "UPDATED", LocalDate.now());
//        HashMap<String, String> bookUpdates = new HashMap<>();
//        bookUpdates.put("title", bookUpdate.getTitle());
//        bookUpdates.put("genre", bookUpdate.getGenre());
//        Mockito.when(bookService.updateBook(book.getBookUUID(), bookUpdates)).thenReturn(ResponseEntity.status(200).location(URI.create("http://localhost:8083/api/book/" + book.getBookUUID())).contentType(MediaType.APPLICATION_JSON).body(bookUpdate));
//        mockMvc.perform(put("/api/book/" + book.getBookUUID()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsBytes(bookUpdates))).andExpect(status().is(200)).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(header().string("Location", "http://localhost:8083/api/book/" + bookUpdate.getBookUUID())).andExpect(content().json(objectMapper.writeValueAsString(bookUpdate)));
//    }
//
//    @Test
//    void studentBorrowBooks() {
//    }
//
//    @Test
//    void returnStudentBorrowBooks() {
//    }
//
//    @Test
//    void bookStudentBorrow() {
//    }
//}