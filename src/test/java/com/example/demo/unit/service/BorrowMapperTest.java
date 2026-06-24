package com.example.demo.unit.service;

import com.example.demo.dto.BorrowAggregate;
import com.example.demo.dto.BorrowCreatedSummaryDTO;
import com.example.demo.dto.LoanItemDetails;
import com.example.demo.mapper.BorrowMapper;
import com.example.demo.model.Borrow;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class BorrowMapperTest {

    @InjectMocks
    private BorrowMapper borrowMapper;


    @Test
    void toEntities_should_map_aggregate_to_borrow_entities() {

        UUID borrowUuid = UUID.randomUUID();
        UUID memberCardUuid = UUID.randomUUID();

        List<LoanItemDetails> items = List.of(new LoanItemDetails(UUID.randomUUID(), UUID.randomUUID()));

        BorrowAggregate aggregate = new BorrowAggregate(borrowUuid, memberCardUuid, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15), items);

        List<Borrow> result = borrowMapper.toEntities(aggregate);

        assertThat(result).hasSize(1);

        Borrow borrow = result.getFirst();

        assertThat(borrow.getBorrowUuid()).isEqualTo(borrowUuid);

        assertThat(borrow.getMemberCardUuid()).isEqualTo(memberCardUuid);

        assertThat(borrow.getBookUuid()).isEqualTo(items.getFirst().book_uuid());

        assertThat(borrow.getChapterUuid()).isEqualTo(items.getFirst().chapter_uuid());
    }


    @Test
    void toSummaryDTO_should_map_aggregate() {

        BorrowAggregate aggregate = Instancio.create(BorrowAggregate.class);

        BorrowCreatedSummaryDTO result = borrowMapper.toSummaryDTO(aggregate);

        assertThat(result.borrow_uuid()).isEqualTo(aggregate.borrowUuid());

        assertThat(result.memberCardUuid()).isEqualTo(aggregate.memberCardUuid());
    }

    @Test
    void toSummaryDTO_should_throw_when_aggregate_is_null() {

        assertThrows(IllegalArgumentException.class, () -> borrowMapper.toSummaryDTO(null));
    }

    @Test
    void toSummaryDTO_should_throw_when_items_are_empty() {
        BorrowAggregate aggregate = new BorrowAggregate(UUID.randomUUID(), UUID.randomUUID(), LocalDate.now(), LocalDate.now().plusWeeks(2), List.of());
        assertThrows(IllegalArgumentException.class, () -> borrowMapper.toSummaryDTO(aggregate));
    }

}