package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public record BorrowCreatedEvent(
        Metadata metadata,

        BorrowCreatedEventData data
) {
}