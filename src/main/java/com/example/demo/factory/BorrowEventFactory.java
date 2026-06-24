package com.example.demo.factory;

import com.example.demo.dto.*;
import com.example.demo.mapper.BorrowMapper;
import com.example.demo.model.Borrow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BorrowEventFactory {

    private final BorrowMapper borrowMapper;

    public BorrowCreatedEvent create(BorrowAggregate aggregate, List<Borrow> entities, UUID eventId) {
        List<BookChapterReference> refs = entities.stream().map(e -> new BookChapterReference(e.getBookUuid(), e.getChapterUuid())).toList();
        BorrowCreatedEventData data = borrowMapper.toEventData(aggregate.memberCardUuid(), aggregate.borrowUuid(), aggregate.startDate(), aggregate.endDate(), refs);
        Metadata metadata = new Metadata(LocalDateTime.now().toString(), "library-app-borrow-v2", "BORROW_CREATED", eventId);
        return new BorrowCreatedEvent(metadata, data);
    }
}