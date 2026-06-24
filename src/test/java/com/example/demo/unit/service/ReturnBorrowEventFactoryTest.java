package com.example.demo.unit.factory;

import com.example.demo.dto.BookToDecrement;
import com.example.demo.dto.ReturnBorrowAggregate;
import com.example.demo.dto.ReturnCreatedEvent;
import com.example.demo.dto.ReturnCreatedEventData;
import com.example.demo.factory.ReturnBorrowEventFactory;
import com.example.demo.mapper.ReturnMapper;
import com.example.demo.model.Borrow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReturnBorrowEventFactoryTest {

    @Mock
    private ReturnMapper returnMapper;
    @InjectMocks
    private ReturnBorrowEventFactory factory;

    @Test
    @DisplayName("Should correctly map Aggregate and Entities to ReturnCreatedEvent")
    void shouldCreateReturnEventSuccessfully() {
        UUID eventId = UUID.randomUUID();
        ReturnBorrowAggregate aggregate = createSampleAggregate();
        List<Borrow> entities = List.of(mock(Borrow.class));
        List<BookToDecrement> decs = List.of(new BookToDecrement(UUID.randomUUID(), UUID.randomUUID()));
        ReturnCreatedEventData expectedData = new ReturnCreatedEventData(aggregate.memberCardUuid(), aggregate.borrowUuid(), aggregate.startDate(), aggregate.endDate(), aggregate.returnDate(), aggregate.isLate(), aggregate.daysLate(), aggregate.fineAmount(), decs);
        when(returnMapper.toBookToDecrement(entities)).thenReturn(decs);
        when(returnMapper.toEventData(any(), any(), any(), any(), any(), anyBoolean(), anyLong(), any(), any())).thenReturn(expectedData);
        ReturnCreatedEvent result = factory.create(aggregate, entities, eventId);
        assertNotNull(result);
        assertEquals(expectedData, result.data());
        assertEquals(eventId, result.metadata().event_uuid());
        assertEquals("RETURN_BORROW_CREATED", result.metadata().event_type());
        assertDoesNotThrow(() -> LocalDateTime.parse(result.metadata().timestamp()));
    }

    private ReturnBorrowAggregate createSampleAggregate() {
        return new ReturnBorrowAggregate(UUID.randomUUID(), UUID.randomUUID(), "2026-01-01", "2026-01-14", "2026-01-15", true, 1L, new BigDecimal("5.00"), List.of());
    }
}