package com.example.demo.unit.service;

import com.example.demo.dto.BookPayload;
import com.example.demo.dto.BorrowAggregate;
import com.example.demo.dto.BorrowCreatedEvent;
import com.example.demo.dto.BorrowCreatedSummaryDTO;
import com.example.demo.exception.DailyBorrowLimitExceededException;
import com.example.demo.exception.UnreturnedBorrowExistsException;
import com.example.demo.mapper.BorrowMapper;
import com.example.demo.model.Borrow;
import com.example.demo.repository.BorrowRepository;
import com.example.demo.service.BorrowEventPublisher;
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

    @Mock
    private BorrowMapper borrowMapper;


    @Mock
    private BorrowEventPublisher borrowEventPublisher;

    @InjectMocks
    private LoanService loanService;

    private BookPayload bookPayload;
    private Borrow borrow;
    private BorrowCreatedEvent borrowCreatedEvent;

    @BeforeEach
    void setUp() {
        bookPayload = Instancio.create(BookPayload.class);
        borrow = Instancio.create(Borrow.class);
        borrowCreatedEvent = Instancio.create(BorrowCreatedEvent.class);
    }

    @Test
    void borrowBooks_should_successfully_borrow_books_when_user_has_no_existing_borrow() {
        UUID memberCardUuid = UUID.randomUUID();
        when(borrowRepository.getBookMemberCardByBook(memberCardUuid)).thenReturn(false);
        when(borrowRepository.existsByMemberCardUuid(memberCardUuid)).thenReturn(false);
        List<Borrow> entities = List.of(borrow);
        when(borrowMapper.toEntities(any(BorrowAggregate.class))).thenReturn(entities);
        when(borrowRepository.saveAll(entities)).thenReturn(entities);
        BorrowCreatedSummaryDTO expectedDto = new BorrowCreatedSummaryDTO(UUID.randomUUID(), memberCardUuid, LocalDate.now().toString(), LocalDate.now().plusWeeks(2).toString(), List.of());
        when(borrowMapper.toSummaryDTO(any(BorrowAggregate.class))).thenReturn(expectedDto);
        BorrowCreatedSummaryDTO result = loanService.borrowBooks(memberCardUuid, bookPayload);
        verify(borrowRepository).saveAll(entities);
        verify(borrowEventPublisher).publishBorrowCreated(any(BorrowAggregate.class), eq(entities));
        assertThat(result).isNotNull();
        assertThat(result.memberCardUuid()).isEqualTo(memberCardUuid);
    }


    @Test
    void borrowBooks_getBookMemberCardByBook_true() {
        UUID uuid = UUID.fromString("030b5fb3-2266-41c3-a016-5f800ef39142");
        when(borrowRepository.getBookMemberCardByBook(uuid)).thenReturn(true);
        UnreturnedBorrowExistsException exception = Assertions.assertThrows(UnreturnedBorrowExistsException.class, () -> loanService.borrowBooks(uuid, bookPayload));
        verify(borrowRepository, times(1)).getBookMemberCardByBook(uuid);
        Assertions.assertEquals("まだ貸出返却されていないの貸し出しがあります。", exception.getMessage());
    }

    @Test
    void borrowBooks_existsByMemberCardUuid_true() {
        UUID uuid = UUID.randomUUID();
        when(borrowRepository.existsByMemberCardUuid(uuid)).thenReturn(true);
        when(borrowRepository.checkLatestBorrowDate(uuid)).thenReturn(true);
        DailyBorrowLimitExceededException exception = Assertions.assertThrows(DailyBorrowLimitExceededException.class, () -> {
            loanService.borrowBooks(uuid, bookPayload);
        });
        verify(borrowRepository, times(1)).getBookMemberCardByBook(uuid);
        Assertions.assertEquals("一日の借入限度額に達しました。", exception.getMessage());
    }

    @Test
    void borrowBooks_should_successfully_borrow_books() {
        UUID memberCardUuid = UUID.randomUUID();
        when(borrowRepository.getBookMemberCardByBook(memberCardUuid)).thenReturn(false);
        when(borrowRepository.existsByMemberCardUuid(memberCardUuid)).thenReturn(false);
        List<Borrow> entities = List.of(borrow);
        when(borrowMapper.toEntities(any(BorrowAggregate.class))).thenReturn(entities);
        when(borrowRepository.saveAll(entities)).thenReturn(entities);
        BorrowCreatedSummaryDTO expectedDto = new BorrowCreatedSummaryDTO(UUID.randomUUID(), memberCardUuid, LocalDate.now().toString(), LocalDate.now().plusWeeks(2).toString(), List.of());
        when(borrowMapper.toSummaryDTO(any(BorrowAggregate.class))).thenReturn(expectedDto);
        BorrowCreatedSummaryDTO response = loanService.borrowBooks(memberCardUuid, bookPayload);
        assertThat(response).isNotNull();
        assertThat(response.memberCardUuid()).isEqualTo(memberCardUuid);
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