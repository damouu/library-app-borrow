package com.example.demo.unit.service;

import com.example.demo.controller.BorrowController;
import com.example.demo.dto.BookPayload;
import com.example.demo.dto.BorrowCreatedSummaryDTO;
import com.example.demo.service.LoanService;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class BorrowControllerTest {

    @Mock
    private LoanService loanService;

    private UUID borrowUUID = UUID.randomUUID();
    private UUID memberCardUUID = UUID.randomUUID();

    private BookPayload bookPayload = Instancio.create(BookPayload.class);

    @Mock
    private Jwt jwt;

    @InjectMocks
    private BorrowController borrowController;

    @Test
    @DisplayName("Should allow user to borrow books")
    void shouldBorrowBooksSuccessfully() {
        UUID memberCardUuid = UUID.randomUUID();
        BorrowCreatedSummaryDTO expectedDto = new BorrowCreatedSummaryDTO(UUID.randomUUID(), memberCardUuid, LocalDate.now().toString(), LocalDate.now().plusWeeks(2).toString(), List.of());
        when(jwt.getClaimAsString("user_memberCardUUID")).thenReturn(memberCardUuid.toString());
        when(loanService.borrowBooks(memberCardUuid, bookPayload)).thenReturn(expectedDto);
        ResponseEntity<BorrowCreatedSummaryDTO> response = borrowController.postBorrowBooks(bookPayload, jwt, memberCardUuid);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(expectedDto);
        verify(loanService).borrowBooks(memberCardUuid, bookPayload);
    }

    @Test
    @DisplayName("Should reject borrow request when member card UUID does not match JWT")
    void shouldRejectBorrowRequestWhenJwtDoesNotMatchMemberCard() {
        when(jwt.getClaimAsString("user_memberCardUUID")).thenReturn(String.valueOf(UUID.randomUUID()));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> borrowController.postBorrowBooks(bookPayload, jwt, memberCardUUID));
        assertEquals(403, exception.getStatus().value());
        verify(loanService, never()).borrowBooks(any(), any());

    }

    @Test
    @DisplayName("Should allow authenticated user to return borrowed books")
    void shouldReturnBorrowedBooksWhenJwtMatchesMemberCard() {
        Map<String, Serializable> params = Map.of("page", 0, "size", 10);
        Map<String, Map<String, Serializable>> response = Map.of("pagination", params);
        ResponseEntity<Map<String, Map<String, Serializable>>> expectedResponse = ResponseEntity.ok(response);
        when(jwt.getClaimAsString("user_memberCardUUID")).thenReturn(memberCardUUID.toString());
        when(loanService.returnBorrowBooks(memberCardUUID, borrowUUID, bookPayload)).thenReturn(expectedResponse);
        ResponseEntity<?> response2 = borrowController.returnBorrowBooks(memberCardUUID, borrowUUID, bookPayload, jwt);
        assertEquals(expectedResponse.getStatusCode(), response2.getStatusCode());
    }

    @Test
    @DisplayName("Should reject return request when member card UUID does not match JWT")
    void shouldRejectReturnRequestWhenJwtDoesNotMatchMemberCard() {
        when(jwt.getClaimAsString("user_memberCardUUID")).thenReturn(String.valueOf(UUID.randomUUID()));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> borrowController.returnBorrowBooks(memberCardUUID, borrowUUID, bookPayload, jwt));
        assertEquals(403, exception.getStatus().value());
        verify(loanService, never()).returnBorrowBooks(any(), any(), any());
    }
}