package com.example.demo.controller;

import com.example.demo.model.MemberCard;
import com.example.demo.service.MemberCardService;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@Data
@Validated
@CrossOrigin(allowedHeaders = "*", origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
@RestController
@RequestMapping("api/memberCard")
public class MemberCardController {

    private final MemberCardService memberCardService;

    @DeleteMapping(value = "/{memberCardUUID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteMemberCard(@PathVariable("memberCardUUID") UUID memberCardUUID, @AuthenticationPrincipal Jwt jwt) {

        String jwtMemberCard = jwt.getClaimAsString("user_memberCardUUID");

        if (!jwtMemberCard.equals(memberCardUUID.toString())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("memberCar UUID mismatch");
        }

        return memberCardService.deleteMemberCard(memberCardUUID);
    }

    @PostMapping(path = "/{memberCardUUID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MemberCard> postMemberCard(@PathVariable("memberCardUUID") UUID memberCardUUID) {
        return memberCardService.postMemberCard(memberCardUUID);
    }

    @PostMapping(path = "/{memberCardUUID}/borrow", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> postBorrowBooks(@PathVariable("memberCardUUID") UUID memberCardUUID, @RequestBody Map<Object, ArrayList<UUID>> booksArrayJson, @AuthenticationPrincipal Jwt jwt) {

        String jwtMemberCard = jwt.getClaimAsString("user_memberCardUUID");

        if (!jwtMemberCard.equals(memberCardUUID.toString())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("memberCar UUID mismatch");
        }

        return memberCardService.borrowBooks(memberCardUUID, booksArrayJson);
    }

    @PostMapping(path = "/{memberCardUUID}/borrow/{borrowUUID}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> returnBorrowBooks(@PathVariable("memberCardUUID") UUID memberCardUUID, @PathVariable("borrowUUID") UUID borrowUUID, @AuthenticationPrincipal Jwt jwt) {

        String jwtMemberCard = jwt.getClaimAsString("user_memberCardUUID");

        if (!jwtMemberCard.equals(memberCardUUID.toString())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("memberCar UUID mismatch");
        }

        return memberCardService.returnBorrowBooks(memberCardUUID, borrowUUID);
    }

    @GetMapping(path = "/{memberCardUUID}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getHistory(@PathVariable("memberCardUUID") UUID memberCardUUID, @AuthenticationPrincipal Jwt jwt, @RequestParam Map<String, ?> allParams) {

//        String jwtMemberCard = jwt.getClaimAsString("user_memberCardUUID");

//        if (!jwtMemberCard.equals(memberCardUUID.toString())) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("memberCar UUID mismatch");
//        }

        return memberCardService.getHistory(memberCardUUID, allParams);
    }

}
