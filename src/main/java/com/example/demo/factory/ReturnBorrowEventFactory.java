package com.example.demo.factory;

import com.example.demo.dto.*;
import com.example.demo.mapper.ReturnMapper;
import com.example.demo.model.Borrow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReturnBorrowEventFactory {

    private final ReturnMapper returnMapper;

    public ReturnCreatedEvent create(ReturnBorrowAggregate aggregate, List<Borrow> entities, UUID eventId) {
        List<BookToDecrement> refs = returnMapper.toBookToDecrement(entities);
        ReturnCreatedEventData data = returnMapper.toEventData(aggregate.memberCardUuid(), aggregate.borrowUuid(), aggregate.startDate(), aggregate.endDate(), aggregate.returnDate(), aggregate.isLate(), aggregate.daysLate(), aggregate.fineAmount(), refs);
        Metadata metadata = new Metadata(LocalDateTime.now().toString(), "library-app-borrow-v2", "RETURN_BORROW_CREATED", eventId);
        return new ReturnCreatedEvent(metadata, data);
    }
}