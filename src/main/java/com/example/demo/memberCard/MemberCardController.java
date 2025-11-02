package com.example.demo.memberCard;

import lombok.Data;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Data
@Validated
@CrossOrigin(allowedHeaders = "*", origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE})
@RestController
@RequestMapping("api/memberCard")
public class MemberCardController {

    private final MemberCardService memberCardService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MemberCard>> getMemberCards(@RequestParam(name = "page", required = false, defaultValue = "0") Integer page, @RequestParam(name = "size", required = false, defaultValue = "5") Integer size) {
        return memberCardService.getMemberCards(page, size);
    }

    @GetMapping(path = "{memberCardUUID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LinkedHashMap<String, Object>> getMemberCard(@PathVariable("memberCardUUID") UUID memberCardUUID) {
        return memberCardService.getMemberCard(memberCardUUID);
    }

    @DeleteMapping(value = "/{memberCardUUID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteMemberCard(@PathVariable("memberCardUUID") UUID memberCardUUID) {
        return memberCardService.deleteMemberCard(memberCardUUID);
    }

    @PostMapping(path = "/{memberCardUUID}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MemberCard> postMemberCard(@PathVariable("memberCardUUID") UUID memberCardUUID) {
        return memberCardService.postMemberCard(memberCardUUID);
    }

    @PostMapping(path = "/{memberCardUUID}/borrow", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> postBorrowBooks(@PathVariable("memberCardUUID") UUID memberCardUUID, @RequestBody Map<Object, ArrayList<UUID>> booksArrayJson) {
        return memberCardService.borrowBooks(memberCardUUID, booksArrayJson);
    }

    @PostMapping(path = "/{memberCardUUID}/borrow/{borrowUUID}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> returnBorrowBooks(@PathVariable("memberCardUUID") UUID memberCardUUID, @PathVariable("borrowUUID") UUID borrowUUID) {
        return memberCardService.returnBorrowBooks(memberCardUUID, borrowUUID);
    }

    @GetMapping(path = "/{memberCardUUID}/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getHistory(@PathVariable("memberCardUUID") UUID memberCardUUID) {
        return memberCardService.getHistory(memberCardUUID);
    }

}
