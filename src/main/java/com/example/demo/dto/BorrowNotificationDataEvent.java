package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BorrowNotificationDataEvent {

    private UUID borrow_uuid;
    private String borrow_start_date;
    private String borrow_end_date;

    private List<ChapterDetails> chapters;
}