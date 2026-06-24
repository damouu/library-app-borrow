package com.example.demo.unit.service;

import com.example.demo.dto.*;
import com.example.demo.event.publisher.KafkaBorrowEventPublisher;
import com.example.demo.exception.DailyBorrowLimitExceededException;
import com.example.demo.exception.UnreturnedBorrowExistsException;
import com.example.demo.mapper.BorrowAssembler;
import com.example.demo.mapper.BorrowMapper;
import com.example.demo.mapper.ReturnBorrowAssembler;
import com.example.demo.mapper.ReturnMapper;
import com.example.demo.model.Borrow;
import com.example.demo.policy.BorrowPolicy;
import com.example.demo.policy.ReturnBorrowPolicy;
import com.example.demo.repository.BorrowRepository;
import com.example.demo.service.LoanService;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private BorrowRepository borrowRepository;

    @Mock
    private KafkaTemplate<UUID, Object> kafkaTemplate;

    @Mock
    private BorrowMapper borrowMapper;

    @Mock
    private ReturnMapper returnMapper;

    @Mock
    private ReturnBorrowAssembler returnBorrowAssembler;

    @Mock
    private ReturnBorrowPolicy returnBorrowPolicy;

    @Mock
    private BorrowAssembler borrowAssembler;

    @Mock
    private BorrowPolicy borrowPolicy;

    @Mock
    private KafkaBorrowEventPublisher borrowEventPublisher;

    @InjectMocks
    private LoanService loanService;

    @Captor
    private ArgumentCaptor<UUID> uuidCaptor;
    @Captor
    private ArgumentCaptor<LocalDate> startDateCaptor;
    @Captor
    private ArgumentCaptor<LocalDate> endDateCaptor;

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
    @DisplayName("Should create borrow successfully")
    void shouldCreateBorrowSuccessfully() {
        UUID memberCardUUID = UUID.randomUUID();
        UUID mockBorrowUUID = UUID.randomUUID();
        LoanItemDetails item = new LoanItemDetails(UUID.randomUUID(), UUID.randomUUID());
        List<LoanItemDetails> items = List.of(item);
        BookPayload payload = new BookPayload(items);
        LocalDate expectedStartDate = LocalDate.now();
        LocalDate expectedEndDate = expectedStartDate.plusWeeks(2);
        BorrowAggregate aggregate = new BorrowAggregate(mockBorrowUUID, memberCardUUID, expectedStartDate, expectedEndDate, items);
        List<Borrow> entities = List.of(mock(Borrow.class));
        BorrowCreatedSummaryDTO expectedDto = new BorrowCreatedSummaryDTO(mockBorrowUUID, memberCardUUID, expectedStartDate.toString(), expectedEndDate.toString(), items);
        when(borrowRepository.getBookMemberCardByBook(memberCardUUID)).thenReturn(false);
        when(borrowRepository.existsByMemberCardUuid(memberCardUUID)).thenReturn(false);
        when(borrowRepository.checkLatestBorrowDate(memberCardUUID)).thenReturn(false);
        doReturn(aggregate).when(borrowAssembler).toAggregate(any(UUID.class), eq(memberCardUUID), any(LocalDate.class), any(LocalDate.class), eq(items));
        when(borrowMapper.toEntities(aggregate)).thenReturn(entities);
        when(borrowMapper.toSummaryDTO(aggregate)).thenReturn(expectedDto);
        BorrowCreatedSummaryDTO result = loanService.borrowBooks(memberCardUUID, payload);
        assertEquals(expectedDto, result);
        verify(borrowPolicy).validateNoActiveBorrow(false);
        verify(borrowPolicy).validateDailyLimit(false, false);
        verify(borrowRepository).saveAll(entities);
        verify(borrowEventPublisher).publishBorrowCreated(aggregate, entities);
        verify(borrowAssembler).toAggregate(uuidCaptor.capture(), eq(memberCardUUID), startDateCaptor.capture(), endDateCaptor.capture(), eq(items));
        assertNotNull(uuidCaptor.getValue(), "Generated Borrow UUID should not be null");
        assertEquals(expectedStartDate, startDateCaptor.getValue(), "Start date should be today");
        assertEquals(expectedEndDate, endDateCaptor.getValue(), "End date should be exactly 2 weeks from today");
    }


    @Test
    void borrowBooks_should_throw_when_daily_limit_reached() {
        UUID uuid = UUID.randomUUID();
        when(borrowRepository.getBookMemberCardByBook(uuid)).thenReturn(false);
        when(borrowRepository.existsByMemberCardUuid(uuid)).thenReturn(true);
        when(borrowRepository.checkLatestBorrowDate(uuid)).thenReturn(true);
        doThrow(new DailyBorrowLimitExceededException("一日の借入限度額に達しました。")).when(borrowPolicy).validateDailyLimit(true, true);
        DailyBorrowLimitExceededException exception = assertThrows(DailyBorrowLimitExceededException.class, () -> loanService.borrowBooks(uuid, bookPayload));
        verify(borrowRepository).existsByMemberCardUuid(uuid);
        verify(borrowRepository).checkLatestBorrowDate(uuid);
        assertThat(exception.getMessage()).isEqualTo("一日の借入限度額に達しました。");
    }

    @Test
    void borrowBooks_should_throw_when_active_borrow_exists() {
        UUID uuid = UUID.randomUUID();
        when(borrowRepository.getBookMemberCardByBook(uuid)).thenReturn(true);
        doThrow(new UnreturnedBorrowExistsException("まだ貸出返却されていないの貸し出しがあります。")).when(borrowPolicy).validateNoActiveBorrow(true);
        UnreturnedBorrowExistsException exception = assertThrows(UnreturnedBorrowExistsException.class, () -> loanService.borrowBooks(uuid, bookPayload));
        verify(borrowRepository).getBookMemberCardByBook(uuid);
        assertThat(exception.getMessage()).isEqualTo("まだ貸出返却されていないの貸し出しがあります。");
    }


    @Test
    void returnBorrowBooks_should_return_borrow_without_late_fee() {
        UUID borrowUuid = UUID.randomUUID();
        UUID memberCardUUID = UUID.randomUUID();
        ReturnBorrowAggregate aggregate = new ReturnBorrowAggregate(borrowUuid, memberCardUUID, borrow.getBorrowStartDate().toString(), borrow.getBorrowEndDate().toString(), LocalDate.now().toString(), false, 0L, BigDecimal.ZERO, List.of());
        when(returnBorrowAssembler.toAggregate(any(), any(), any(), any(), anyBoolean(), anyLong(), any(), anyList())).thenReturn(aggregate);
        Borrow borrow = Instancio.of(Borrow.class).set(field(Borrow::getBorrowStartDate), LocalDate.now()).set(field(Borrow::getBorrowEndDate), LocalDate.now().plusWeeks(2)).create();
        List<Borrow> borrows = List.of(borrow);
        when(borrowRepository.getBorrowsByBorrowUuidAndMemberCardUuidAndBorrowReturnDateIsNull(borrowUuid, memberCardUUID)).thenReturn(borrows);
        when(returnBorrowPolicy.validateAndGetBorrow(borrows)).thenReturn(borrow);
        when(returnBorrowPolicy.calculateDaysLate(any(), any())).thenReturn(0L);
        when(returnBorrowPolicy.isLate(0L)).thenReturn(false);
        when(returnBorrowPolicy.calculateFine(eq(0L), any())).thenReturn(BigDecimal.ZERO);
        ReturnBorrowCreatedSummaryDTO expected = new ReturnBorrowCreatedSummaryDTO(borrowUuid, memberCardUUID, borrow.getBorrowStartDate().toString(), borrow.getBorrowEndDate().toString(), LocalDate.now().toString(), false, 0L, BigDecimal.ZERO, List.of());
        when(returnMapper.toSummaryDTO(any(ReturnBorrowAggregate.class))).thenReturn(expected);
        ReturnBorrowCreatedSummaryDTO response = loanService.returnBorrowBooks(memberCardUUID, borrowUuid, bookPayload);
        verify(borrowRepository).setReturnDateForBorrows(eq(borrows), any(LocalDate.class));
        verify(borrowEventPublisher).publishReturnBorrowCreated(any(ReturnBorrowAggregate.class), eq(borrows));
        assertThat(response).isEqualTo(expected);
    }

    @Test
    void returnBorrowBooks_should_successfully_return_books() {
        UUID borrowUuid = UUID.randomUUID();
        UUID memberCardUUID = UUID.randomUUID();
        LocalDate currentDate = LocalDate.now();
        Borrow borrow = Instancio.create(Borrow.class);
        List<Borrow> borrows = List.of(borrow);
        List<LoanItemDetails> items = List.of(new LoanItemDetails(UUID.randomUUID(), UUID.randomUUID()));
        ReturnBorrowAggregate aggregate = new ReturnBorrowAggregate(borrowUuid, memberCardUUID, borrow.getBorrowStartDate().toString(), borrow.getBorrowEndDate().toString(), currentDate.toString(), true, 5L, BigDecimal.valueOf(500), items);
        ReturnBorrowCreatedSummaryDTO expectedDto = new ReturnBorrowCreatedSummaryDTO(borrowUuid, memberCardUUID, borrow.getBorrowStartDate().toString(), borrow.getBorrowEndDate().toString(), currentDate.toString(), true, 5L, BigDecimal.valueOf(500), items);
        when(borrowRepository.getBorrowsByBorrowUuidAndMemberCardUuidAndBorrowReturnDateIsNull(borrowUuid, memberCardUUID)).thenReturn(borrows);
        when(returnBorrowPolicy.validateAndGetBorrow(borrows)).thenReturn(borrow);
        when(returnBorrowPolicy.calculateDaysLate(any(), any())).thenReturn(5L);
        when(returnBorrowPolicy.isLate(5L)).thenReturn(true);
        when(returnBorrowPolicy.calculateFine(eq(5L), any())).thenReturn(BigDecimal.valueOf(500));
        when(returnBorrowAssembler.toAggregate(eq(borrow), eq(borrowUuid), eq(memberCardUUID), any(LocalDate.class), eq(true), eq(5L), eq(BigDecimal.valueOf(500)), anyList())).thenReturn(aggregate);
        when(returnMapper.toSummaryDTO(aggregate)).thenReturn(expectedDto);
        ReturnBorrowCreatedSummaryDTO result = loanService.returnBorrowBooks(memberCardUUID, borrowUuid, bookPayload);
        verify(borrowRepository).getBorrowsByBorrowUuidAndMemberCardUuidAndBorrowReturnDateIsNull(borrowUuid, memberCardUUID);
        verify(borrowRepository).setReturnDateForBorrows(eq(borrows), any(LocalDate.class));
        verify(borrowEventPublisher).publishReturnBorrowCreated(aggregate, borrows);
        assertThat(result).isEqualTo(expectedDto);
    }


    @Test
    void returnBorrowBooks_should_throw_exception_when_borrow_not_found() {
        UUID borrowUuid = UUID.randomUUID();
        UUID memberCardUUID = UUID.randomUUID();
        when(borrowRepository.getBorrowsByBorrowUuidAndMemberCardUuidAndBorrowReturnDateIsNull(borrowUuid, memberCardUUID)).thenReturn(List.of());
        when(returnBorrowPolicy.validateAndGetBorrow(anyList())).thenThrow(new UnreturnedBorrowExistsException("まだ貸出返却されていないの貸し出しがあります。"));
        assertThrows(UnreturnedBorrowExistsException.class, () -> loanService.returnBorrowBooks(memberCardUUID, borrowUuid, bookPayload));
        verify(borrowRepository, never()).setReturnDateForBorrows(any(), any());
        verify(borrowEventPublisher, never()).publishReturnBorrowCreated(any(), any());
    }
}