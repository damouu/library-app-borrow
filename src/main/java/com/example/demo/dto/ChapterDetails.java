package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ChapterDetails {

    @JsonProperty("chapter_title")
    private String chapterTitle;

    @JsonProperty("chapter_number")
    private int chapterNumber;

    @JsonProperty("chapter_uuid")
    private UUID chapterUUID;

    @JsonProperty("chapter_second_title")
    private String chapterSecondTitle;

    @JsonProperty("chapter_cover_url")
    private String chapterCoverUrl;

}
