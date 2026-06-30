package com.example.demo.dto;

import java.util.UUID;


public record BookChapterReference(
        UUID book_uuid,

        UUID chapter_uuid
) {
}
