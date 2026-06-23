package com.example.demo.dto;

import java.util.UUID;


public record Metadata(
        String timestamp,

        String source_service,

        String event_type,

        UUID event_uuid
) {
}