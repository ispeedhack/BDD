package com.eritten.backend.contacts;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eritten.backend.models.Blacklist;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/blacklist")
@AllArgsConstructor
public class BlacklistController {

    private final BlacklistService blacklistService;

    @PostMapping("/block")
    public ResponseEntity<BlacklistResponse> blockContact(@RequestBody BlacklistRequest request) {
        BlacklistResponse response = blacklistService.blockContact(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/unblock")
    public ResponseEntity<BlacklistResponse> unblockContact(@RequestBody BlacklistRequest request) {
        BlacklistResponse response = blacklistService.unblockContact(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/blocked-contacts")
    public ResponseEntity<List<Blacklist>> getBlockedContacts(@RequestParam String userEmail) {
        List<Blacklist> blockedContacts = blacklistService.getBlockedContacts(userEmail);
        return new ResponseEntity<>(blockedContacts, HttpStatus.OK);
    }
}
