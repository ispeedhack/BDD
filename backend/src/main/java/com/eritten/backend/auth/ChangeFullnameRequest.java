package com.eritten.backend.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class ChangeFullnameRequest {
    private final String newFullname;
    private final String email;

}
