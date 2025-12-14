package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturnNotificationDataEvent {

    private UUID borrow_uuid;
    private String borrow_start_date;
    private String borrow_end_date;
    private String borrow_return_date;
    private Boolean return_lately;
    private Integer days_late;
    private BigDecimal late_fee;

    private List<ChapterDetails> chapters;
}