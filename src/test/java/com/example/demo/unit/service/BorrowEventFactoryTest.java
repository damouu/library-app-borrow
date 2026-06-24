package com.example.demo.unit.factory;

import com.example.demo.dto.BookChapterReference;
import com.example.demo.dto.BorrowAggregate;
import com.example.demo.dto.BorrowCreatedEvent;
import com.example.demo.dto.BorrowCreatedEventData;
import com.example.demo.factory.BorrowEventFactory;
import com.example.demo.mapper.BorrowMapper;
import com.example.demo.model.Borrow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowEventFactoryTest {

    @Mock
    private BorrowMapper borrowMapper;

    @InjectMocks
    private BorrowEventFactory borrowEventFactory;

    @Captor
    private ArgumentCaptor<List<BookChapterReference>> refsCaptor;

    @Test
    @DisplayName("Should assemble BorrowCreatedEvent with correct mappings and metadata")
    void shouldCreateEventSuccessfully() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        UUID memberCardUuid = UUID.randomUUID();
        UUID borrowUuid = UUID.randomUUID();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusWeeks(2);

        // 1. Setup Aggregate
        BorrowAggregate aggregate = new BorrowAggregate(borrowUuid, memberCardUuid, startDate, endDate, List.of());

        // 2. Setup Entities
        UUID mockBookUuid = UUID.randomUUID();
        UUID mockChapterUuid = UUID.randomUUID();
        Borrow mockBorrow = mock(Borrow.class);
        when(mockBorrow.getBookUuid()).thenReturn(mockBookUuid);
        when(mockBorrow.getChapterUuid()).thenReturn(mockChapterUuid);

        List<Borrow> entities = List.of(mockBorrow);

        // 3. Instantiate the Real Record instead of a mock
        List<BookChapterReference> expectedRefs = List.of(new BookChapterReference(mockBookUuid, mockChapterUuid));
        BorrowCreatedEventData expectedData = new BorrowCreatedEventData(memberCardUuid, borrowUuid, startDate.toString(), endDate.toString(), expectedRefs);

        // Stub the mapper to return our real record instance
        when(borrowMapper.toEventData(eq(memberCardUuid), eq(borrowUuid), eq(startDate), eq(endDate), anyList())).thenReturn(expectedData);

        // Act
        BorrowCreatedEvent result = borrowEventFactory.create(aggregate, entities, eventId);

        // Assert
        assertNotNull(result, "The generated event should not be null");

        // Verify Metadata
        assertNotNull(result.metadata(), "Event metadata should not be null");
        assertEquals("BORROW_CREATED", result.metadata().event_type(), "Event type should be BORROW_CREATED");
        assertDoesNotThrow(() -> LocalDateTime.parse(result.metadata().timestamp()), "Timestamp should be a valid ISO-8601 string");

        // Verify Payload Data matches our record precisely
        assertEquals(expectedData, result.data(), "Event data should exactly match mapper output");
        assertEquals(memberCardUuid, result.data().member_card_uuid());
        assertEquals(borrowUuid, result.data().borrow_uuid());

        // Verify Internal Stream/Map Logic inside Factory
        verify(borrowMapper).toEventData(eq(memberCardUuid), eq(borrowUuid), eq(startDate), eq(endDate), refsCaptor.capture());

        List<BookChapterReference> capturedRefs = refsCaptor.getValue();
        assertEquals(1, capturedRefs.size(), "Should map exactly one reference");
        assertEquals(mockBookUuid, capturedRefs.get(0).bookUUID(), "Book UUID should map correctly");
        assertEquals(mockChapterUuid, capturedRefs.get(0).chapterUUID(), "Chapter UUID should map correctly");
    }
}