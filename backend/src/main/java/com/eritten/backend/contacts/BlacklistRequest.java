package com.eritten.backend.contacts;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BlacklistRequest {
    private String userEmail;
    private String blockedUserEmail;
}
