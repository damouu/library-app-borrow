package com.example.demo.unit.service;

import com.example.demo.dto.BookToDecrement;
import com.example.demo.dto.ReturnBorrowAggregate;
import com.example.demo.dto.ReturnBorrowCreatedSummaryDTO;
import com.example.demo.dto.ReturnCreatedEventData;
import com.example.demo.mapper.ReturnMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ReturnMapperTest {

    @InjectMocks
    private ReturnMapper returnMapper;

    @Test
    void toSummaryDTO_should_map_aggregate() {
        ReturnBorrowAggregate aggregate = Instancio.create(ReturnBorrowAggregate.class);
        ReturnBorrowCreatedSummaryDTO result = returnMapper.toSummaryDTO(aggregate);
        assertThat(result.borrowUuid()).isEqualTo(aggregate.borrowUuid());
        assertThat(result.memberCardUuid()).isEqualTo(aggregate.memberCardUuid());
        assertThat(result.items()).hasSize(aggregate.items().size());
    }


    @Test
    void toSummaryDTO_should_throw_when_aggregate_is_null() {
        assertThrows(IllegalArgumentException.class, () -> returnMapper.toSummaryDTO(null));
    }

    @Test
    void toEventData_should_map_all_fields() {

        UUID memberCardUuid = UUID.randomUUID();
        UUID borrowUuid = UUID.randomUUID();

        List<BookToDecrement> books = List.of(new BookToDecrement(UUID.randomUUID(), UUID.randomUUID()));

        ReturnCreatedEventData result = returnMapper.toEventData(memberCardUuid, borrowUuid, "2026-01-01", "2026-01-15", "2026-01-20", true, 5L, BigDecimal.valueOf(500), books);

        assertThat(result.member_card_uuid()).isEqualTo(memberCardUuid);

        assertThat(result.borrow_uuid()).isEqualTo(borrowUuid);

        assertThat(result.borrow_start_date()).isEqualTo("2026-01-01");

        assertThat(result.borrow_end_date()).isEqualTo("2026-01-15");

        assertThat(result.borrow_return_date()).isEqualTo("2026-01-20");

        assertThat(result.return_lately()).isTrue();

        assertThat(result.days_late()).isEqualTo(5L);

        assertThat(result.late_fee()).isEqualByComparingTo("500");

    }

}

