package com.eritten.backend.services;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Service;

/**
 * VerificationCodeGeneratorService
 */

@Service
public class VerificationCodeGeneratorService {

    public String generateRandomCode(int length) {
        SecureRandom secureRandom;
        try {
            secureRandom = SecureRandom.getInstanceStrong(); // Use a cryptographically strong random number generator
        } catch (NoSuchAlgorithmException e) {
            // Handle exception
            e.printStackTrace();
            return null;
        }

        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);

        // Convert random bytes to a base64 encoded string
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}