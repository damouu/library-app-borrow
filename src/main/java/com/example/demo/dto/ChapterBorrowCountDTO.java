package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ChapterBorrowCountDTO {
    private final UUID chapterUuid;
    private final Long borrowCount;

    public ChapterBorrowCountDTO(UUID chapterUuid, Long borrowCount) {
        this.chapterUuid = chapterUuid;
        this.borrowCount = borrowCount;
    }
}