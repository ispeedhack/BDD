package com.eritten.backend.contacts;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContactRequest {
    private final String userEmail;
    private final String contactEmail;

}
