package com.example.demo.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookToDecrement {
    private UUID book_uuid;
}
