package com.example.demo.dto;

public record ReturnCreatedEvent(
        Metadata metadata,

        ReturnCreatedEventData data
) {
}