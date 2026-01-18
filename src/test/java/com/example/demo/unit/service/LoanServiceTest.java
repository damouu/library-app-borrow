package com.example.demo.unit.service;

import com.example.demo.dto.BookPayload;
import com.example.demo.dto.BorrowEventPayload;
import com.example.demo.model.Borrow;
import com.example.demo.repository.BorrowRepository;
import com.example.demo.service.KafkaPayloadBuilderService;
import com.example.demo.service.LoanService;
import org.instancio.Instancio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private BorrowRepository borrowRepository;

    @Mock
    private KafkaPayloadBuilderService payloadBuilderService;

    @Mock
    private KafkaTemplate<UUID, Object> kafkaTemplate;

    @InjectMocks
    private LoanService loanService;

    private BookPayload bookPayload;
    private Borrow borrow;
    private BorrowEventPayload borrowEventPayload;

    @BeforeEach
    void setUp() {
        bookPayload = Instancio.create(BookPayload.class);
        borrow = Instancio.create(Borrow.class);
        borrowEventPayload = Instancio.create(BorrowEventPayload.class);
    }

    @Test
    void borrowBooks_existsByMemberCardUuid_true_1() {
        UUID uuid = UUID.randomUUID();
        when(borrowRepository.existsByMemberCardUuid(uuid)).thenReturn(false);
        when(payloadBuilderService.buildBorrowEntities(any(), any(), any(), any(), any())).thenReturn(List.of(borrow));
        when(payloadBuilderService.buildBorrowPayload(any(), any(), any(), any(), any(), any(), any())).thenReturn(borrowEventPayload);
        var response = loanService.borrowBooks(uuid, bookPayload);
        verify(borrowRepository, times(1)).saveAll(anyList());
        verify(kafkaTemplate).send(eq("library.borrow.v1"), any(), any());
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        assertThat(response.getBody()).isInstanceOf(Map.class).extracting("message").isEqualTo(bookPayload.getData().size() + "冊の本は貸し出しされる完了です。");
        assertThat(response.getBody()).isInstanceOf(Map.class).extracting("data").isInstanceOf(Map.class).extracting("borrow_UUID").isNotNull();
        assertThat(response.getBody()).isInstanceOf(Map.class).extracting("data").isInstanceOf(Map.class).extracting("start_borrow_date").isEqualTo(LocalDate.now().toString());
        assertThat(response.getBody()).isInstanceOf(Map.class).extracting("data").isInstanceOf(Map.class).extracting("end_borrow_date").isEqualTo(LocalDate.now().plusWeeks(2).toString());
    }


    @Test
    void borrowBooks_getBookMemberCardByBook_true() {
        UUID uuid = UUID.fromString("030b5fb3-2266-41c3-a016-5f800ef39142");
        when(borrowRepository.getBookMemberCardByBook(uuid)).thenReturn(true);
        ResponseStatusException exception = Assertions.assertThrows(ResponseStatusException.class, () -> {
            loanService.borrowBooks(uuid, bookPayload);
        });
        verify(borrowRepository, times(1)).getBookMemberCardByBook(uuid);
        Assertions.assertTrue(exception.getStatus().is4xxClientError());
        Assertions.assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        Assertions.assertEquals(409 + " CONFLICT \"まだ貸出返却されていないの貸し出しがあります。\"", exception.getMessage());
    }

    @Test
    void borrowBooks_existsByMemberCardUuid_true() {
        UUID uuid = UUID.randomUUID();
        when(borrowRepository.existsByMemberCardUuid(uuid)).thenReturn(true);
        when(borrowRepository.checkLatestBorrowDate(uuid)).thenReturn(true);
        ResponseStatusException exception = Assertions.assertThrows(ResponseStatusException.class, () -> {
            loanService.borrowBooks(uuid, bookPayload);
        });
        verify(borrowRepository, times(1)).getBookMemberCardByBook(uuid);
        Assertions.assertTrue(exception.getStatus().is4xxClientError());
        Assertions.assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        Assertions.assertEquals(403 + " FORBIDDEN \"一日の借入限度額に達しました。\"", exception.getMessage());
    }

    @Test
    void borrowBooks_existsByMemberCardUuid_false() {
        UUID uuid = UUID.randomUUID();
        when(borrowRepository.existsByMemberCardUuid(uuid)).thenReturn(true);
        when(borrowRepository.checkLatestBorrowDate(uuid)).thenReturn(false);
        when(payloadBuilderService.buildBorrowEntities(any(), any(), any(), any(), any())).thenReturn(List.of(borrow));
        when(payloadBuilderService.buildBorrowPayload(any(), any(), any(), any(), any(), any(), any())).thenReturn(borrowEventPayload);
        var response = loanService.borrowBooks(uuid, bookPayload);
        verify(borrowRepository, times(1)).saveAll(anyList());
        verify(kafkaTemplate).send(eq("library.borrow.v1"), any(), any());
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        assertThat(response.getBody()).isInstanceOf(Map.class).extracting("message").isEqualTo(bookPayload.getData().size() + "冊の本は貸し出しされる完了です。");
        assertThat(response.getBody()).isInstanceOf(Map.class).extracting("data").isInstanceOf(Map.class).extracting("borrow_UUID").isNotNull();
        assertThat(response.getBody()).isInstanceOf(Map.class).extracting("data").isInstanceOf(Map.class).extracting("start_borrow_date").isEqualTo(LocalDate.now().toString());
        assertThat(response.getBody()).isInstanceOf(Map.class).extracting("data").isInstanceOf(Map.class).extracting("end_borrow_date").isEqualTo(LocalDate.now().plusWeeks(2).toString());
    }

    @Test
    void returnBorrowBooks_Should_Days_Late_false() {
        UUID borrowUuid = UUID.randomUUID();
        UUID memberCardUUID = UUID.randomUUID();
        List<Borrow> borrows = Instancio.ofList(Borrow.class).size(1).set(field(Borrow::getBorrowStartDate), LocalDate.now()).set(field(Borrow::getBorrowEndDate), LocalDate.now().plusWeeks(2)).set(field(Borrow::getBorrowReturnDate), LocalDate.now().plusWeeks(2)).create();
        when(borrowRepository.getBorrowsByBorrowUuidAndMemberCardUuidAndBorrowReturnDateIsNull(borrowUuid, memberCardUUID)).thenReturn(borrows);
        var response = loanService.returnBorrowBooks(memberCardUUID, borrowUuid, bookPayload);
        verify(borrowRepository, times(1)).setReturnDateForBorrows(eq(borrows), any());
        verify(kafkaTemplate).send("library.return.v1", borrowUuid, null);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        assertThat(response.getBody()).isInstanceOf(Map.class).extracting("data").isInstanceOf(Map.class).extracting("return_lately").isEqualTo(false);
        assertThat(response.getBody()).isInstanceOf(Map.class).extracting("data").isInstanceOf(Map.class).extracting("borrow_UUID").isEqualTo(borrowUuid);
        assertThat(response.getBody()).isInstanceOf(Map.class).extracting("data").isInstanceOf(Map.class).extracting("days_late").isEqualTo(0L);
        assertThat(response.getBody()).isInstanceOf(Map.class).extracting("data").isInstanceOf(Map.class).extracting("fine_amount").isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void returnBorrowBooks_Should_Days_Late_true() {
        UUID borrowUuid = UUID.randomUUID();
        UUID memberCardUUID = UUID.randomUUID();
        List<Borrow> borrows = Instancio.ofList(Borrow.class).size(1).set(field(Borrow::getBorrowStartDate), LocalDate.of(2025, 12, 1)).set(field(Borrow::getBorrowEndDate), LocalDate.of(2025, 12, 1).plusWeeks(2)).set(field(Borrow::getBorrowReturnDate), LocalDate.of(2025, 12, 28)).create();
        when(borrowRepository.getBorrowsByBorrowUuidAndMemberCardUuidAndBorrowReturnDateIsNull(borrowUuid, memberCardUUID)).thenReturn(borrows);
        var response = loanService.returnBorrowBooks(memberCardUUID, borrowUuid, bookPayload);
        verify(borrowRepository, times(1)).setReturnDateForBorrows(borrows, LocalDate.now());
        verify(borrowRepository, times(1)).getBorrowsByBorrowUuidAndMemberCardUuidAndBorrowReturnDateIsNull(borrowUuid, memberCardUUID);
        verify(kafkaTemplate).send("library.return.v1", borrowUuid, null);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        assertThat(response.getBody()).isInstanceOf(Map.class).extracting("data").isInstanceOf(Map.class).extracting("return_lately").isEqualTo(true);
        assertThat(response.getBody()).isInstanceOf(Map.class).extracting("data").isInstanceOf(Map.class).extracting("borrow_UUID").isEqualTo(borrowUuid);
        assertThat(response.getBody()).isInstanceOf(Map.class).extracting("data").isInstanceOf(Map.class).extracting("days_late").isNotNull();
        assertThat(response.getBody()).isInstanceOf(Map.class).extracting("data").isInstanceOf(Map.class).extracting("fine_amount").isNotNull();

    }

    @Test
    void returnBorrowBooks_Should_Borrow_NOT_FOUND() {
        UUID borrowUuid = UUID.randomUUID();
        UUID memberCardUUID = UUID.randomUUID();
        List<Borrow> borrows = Instancio.ofList(Borrow.class).size(0).create();
        when(borrowRepository.getBorrowsByBorrowUuidAndMemberCardUuidAndBorrowReturnDateIsNull(borrowUuid, memberCardUUID)).thenReturn(borrows);
        var response = loanService.returnBorrowBooks(memberCardUUID, borrowUuid, bookPayload);
        verify(borrowRepository, times(0)).setReturnDateForBorrows(eq(borrows), any());
        verify(borrowRepository, times(1)).getBorrowsByBorrowUuidAndMemberCardUuidAndBorrowReturnDateIsNull(borrowUuid, memberCardUUID);
        Assertions.assertTrue(response.getStatusCode().is4xxClientError());
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertThat(response.getBody()).isInstanceOf(Map.class).extracting("message").isInstanceOf(Map.class).extracting("dede").isEqualTo("該当する貸し出しが見つかりませんでした。");
    }
}