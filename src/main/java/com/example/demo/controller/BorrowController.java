package com.example.demo.controller;

import com.example.demo.dto.BookPayload;
import com.example.demo.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@Validated
@RestController
@RequiredArgsConstructor
public class BorrowController {

    private final LoanService loanService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping(path = "/books", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> postBorrowBooks(@RequestBody BookPayload booksArrayJson, @AuthenticationPrincipal Jwt jwt, @RequestHeader("X-User-UUID") UUID memberCardUUID) {

        String jwtMemberCard = jwt.getClaimAsString("user_memberCardUUID");

        if (!jwtMemberCard.equals(memberCardUUID.toString())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "memberCard UUID mismatch");
        }

        return loanService.borrowBooks(memberCardUUID, booksArrayJson);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(path = "/{borrowUUID}/return", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> returnBorrowBooks(@RequestHeader("X-User-UUID") UUID memberCardUUID, @PathVariable UUID borrowUUID, @RequestBody BookPayload booksArrayJson, @AuthenticationPrincipal Jwt jwt) {

        String jwtMemberCard = jwt.getClaimAsString("user_memberCardUUID");

        if (!jwtMemberCard.equals(memberCardUUID.toString())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "memberCard UUID mismatch");
        }

        return loanService.returnBorrowBooks(memberCardUUID, borrowUUID, booksArrayJson);
    }
}
