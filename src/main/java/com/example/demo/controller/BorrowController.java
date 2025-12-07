package com.example.demo.controller;

import com.example.demo.dto.BookPayload;
import com.example.demo.service.BorrowService;
import lombok.Data;
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

@Data
@Validated
@CrossOrigin(allowedHeaders = "*", origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
@RestController
@RequestMapping("api/")
public class BorrowController {

    private final BorrowService borrowService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping(path = "/membercard/{memberCardUUID}/borrow", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> postBorrowBooks(@PathVariable("memberCardUUID") UUID memberCardUUID, @RequestBody BookPayload booksArrayJson, @AuthenticationPrincipal Jwt jwt) {

        String jwtMemberCard = jwt.getClaimAsString("user_memberCardUUID");

        if (!jwtMemberCard.equals(memberCardUUID.toString())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "memberCard UUID mismatch");
        }

        return borrowService.borrowBooks(memberCardUUID, booksArrayJson);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(path = "/membercard/{memberCardUUID}/borrow/{borrowUUID}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> returnBorrowBooks(@PathVariable("memberCardUUID") UUID memberCardUUID, @PathVariable("borrowUUID") UUID borrowUUID, @RequestBody BookPayload booksArrayJson, @AuthenticationPrincipal Jwt jwt) {

        String jwtMemberCard = jwt.getClaimAsString("user_memberCardUUID");

        if (!jwtMemberCard.equals(memberCardUUID.toString())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "memberCard UUID mismatch");
        }

        return borrowService.returnBorrowBooks(memberCardUUID, borrowUUID, booksArrayJson);
    }

    @GetMapping(path = "public/borrow/top-chapters", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> returnBorrowBooks(@RequestParam Map<String, ?> allParams, @RequestParam(defaultValue = "currentweek") String period) {
        return borrowService.topChapters(allParams, period);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/membercard/{memberCardUUID}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getHistory(@PathVariable("memberCardUUID") UUID memberCardUUID, @RequestParam Map<String, ?> allParams, @AuthenticationPrincipal Jwt jwt) {

        String jwtMemberCard = jwt.getClaimAsString("user_memberCardUUID");

        if (!jwtMemberCard.equals(memberCardUUID.toString())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "memberCard UUID mismatch");
        }

        return borrowService.getHistory(memberCardUUID, allParams);
    }

}
