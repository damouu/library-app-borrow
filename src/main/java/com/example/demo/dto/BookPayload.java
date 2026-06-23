package com.example.demo.dto;

import java.util.List;


public record BookPayload(
        List<LoanItemDetails> data
) {
}