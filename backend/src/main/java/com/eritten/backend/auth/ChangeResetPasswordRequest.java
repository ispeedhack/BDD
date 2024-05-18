package com.eritten.backend.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangeResetPasswordRequest {
    private String email;
    private String newPassword;
    private String currentPassword;


}
