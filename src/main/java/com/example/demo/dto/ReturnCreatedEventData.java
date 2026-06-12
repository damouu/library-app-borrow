package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturnCreatedEventData {

    private UUID member_card_uuid;
    private UUID borrow_uuid;
    private String borrow_start_date;
    private String borrow_end_date;
    private String borrow_return_date;
    private Boolean return_lately;
    private Integer days_late;
    private BigDecimal late_fee;

    private List<BookToDecrement> returned_items;
}