package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookChapterReference {

    @JsonProperty("book_uuid")
    private UUID bookUUID;

    @JsonProperty("chapter_uuid")
    private UUID chapterUUID;

}
