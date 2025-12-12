package com.example.demo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class LoanItemDetails {

    private UUID book_uuid;

    private ChapterDetails chapter;
}
