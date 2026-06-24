package com.example.demo.unit.service;

import com.example.demo.exception.BorrowNotFoundException;
import com.example.demo.exception.DailyBorrowLimitExceededException;
import com.example.demo.exception.UnreturnedBorrowExistsException;
import com.example.demo.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestExceptionController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    static class TestController {
        @GetMapping("/test/conflict")
        void throwConflict() {
            throw new UnreturnedBorrowExistsException("Conflict error");
        }

        @GetMapping("/test/forbidden")
        void throwForbidden() {
            throw new DailyBorrowLimitExceededException("Limit exceeded");
        }

        @GetMapping("/test/notfound")
        void throwNotFound() {
            throw new BorrowNotFoundException("Not found");
        }
    }

    @Test
    void should_return_conflict_status_when_unreturned_borrow_exists() throws Exception {
        mockMvc.perform(get("/test/conflict")).andExpect(status().isConflict());
    }

    @Test
    void should_return_forbidden_status_when_limit_exceeded() throws Exception {
        mockMvc.perform(get("/test/forbidden")).andExpect(status().isForbidden());
    }

    @Test
    void should_return_not_found_status_when_borrow_not_found() throws Exception {
        mockMvc.perform(get("/test/notfound")).andExpect(status().isNotFound());
    }
}