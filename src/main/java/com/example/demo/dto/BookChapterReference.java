package com.example.demo.dto;

import java.util.UUID;


public record BookChapterReference(
        UUID bookUUID,

        UUID chapterUUID
) {
}
