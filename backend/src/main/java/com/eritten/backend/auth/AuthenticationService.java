package com.eritten.backend.auth;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.eritten.backend.config.JwtService;
import com.eritten.backend.models.User;
import com.eritten.backend.repositories.UserRepository;
import com.eritten.backend.services.MailService;
import com.eritten.backend.services.VerificationCodeGeneratorService;

import lombok.Data;

@Data
@Service
public class AuthenticationService {
        private final UserRepository repository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;
        private final VerificationCodeGeneratorService verificationCodeGeneratorService;
        private final MailService mailService;

        @Value("${upload-dir}")
        private String uploadDir;

        public AuthenticationResponse register(RegisterRequest request) {
                final String verificationCode = verificationCodeGeneratorService.generateRandomCode(6);
                final String fullname = request.getFullname();
                final String emailMessage = "Email Verification for Signup on Our Social Networking Platform\n\n"
                                +
                                "Dear " + fullname + ",\n\n" +
                                "Thank you for choosing to sign up for our social networking platform. We are excited to have you join our community!\n\n"
                                +
                                "To ensure the security of your account and to complete the signup process, we require email verification. Please use the following code:\n\n"
                                +
                                verificationCode + "\n\n" +
                                "Once you have received the verification code, please enter it in the appropriate field on our website to continue with the signup process.\n\n"
                                +
                                "If you have any questions or encounter any issues during the signup process, please do not hesitate to contact our support team at eritten.gyau@amalitech.com for assistance.\n\n"
                                +
                                "We appreciate your interest in our platform and look forward to seeing you online soon!\n\n"
                                +
                                "Best regards,\n" +
                                "Amalitech";
                Optional<User> existingUserOptional = repository.findByEmail(request.getEmail());
                if (existingUserOptional.isPresent()) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User already exists");
                }

                var user = User.builder()
                                .fullName(request.getFullname())
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .verificationCode(verificationCode)
                                .build();
                repository.save(user);

                mailService.sendEmail(request.getEmail(), "Social networking site email verification", emailMessage);

                var jwtToken = jwtService.generateToken(user);

                return AuthenticationResponse.builder()
                                .accessToken(jwtToken)
                                .build();
        }

        public AuthenticationResponse authenticate(AuthenticationRequest request) {
                authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),
                                request.getPassword()));
                var user = repository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

                // Check if the user is verified
                if (!user.isVerified()) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not verified");
                }

                var jwtToken = jwtService.generateToken(user);
                return AuthenticationResponse.builder()
                        .accessToken(jwtToken)
                        .build();
        }


        public ChangeEmailResponse changeEmail(ChangeEmailRequest request) {
                // Retrieve the current user by email
                Optional<User> currentUserOptional = repository.findByEmail(request.getEmail());

                // Check if the user exists
                if (!currentUserOptional.isPresent()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User does not exist");
                }

                // Get the user from the Optional
                User currentUser = currentUserOptional.get();

                // Update the user's email
                currentUser.setEmail(request.getNewEmail());

                // Save the updated user
                repository.save(currentUser);

                // Create and return the response
                return new ChangeEmailResponse("Email updated successfully");
        }

        public ChangePasswordResponse changePassword(ChangePasswordRequest request) {
                User currentUser = repository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                                "User does not exist"));

                if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid current password");
                }

                String newPasswordEncoded = passwordEncoder.encode(request.getNewPassword());
                currentUser.setPassword(newPasswordEncoded);
                repository.save(currentUser);

                return new ChangePasswordResponse("Password changed successfully");
        }

        public ChangeFullnameResponse changeFullname(ChangeFullnameRequest request) {
                // Retrieve the current user by email
                Optional<User> currentUserOptional = repository.findByEmail(request.getEmail());

                // Check if the user exists
                if (!currentUserOptional.isPresent()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User does not exist");
                }

                // Get the user from the Optional
                User currentUser = currentUserOptional.get();

                // Update the user's full name
                currentUser.setFullName(request.getNewFullname());

                // Save the updated user
                repository.save(currentUser);

                // Create and return the response
                return new ChangeFullnameResponse("Full name is updated successfully");
        }

        public String uploadProfileImage(String email, MultipartFile file) {
                if (file.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select a file to upload");
                }

                try {
                        // Save the file to the server
                        String fileName = file.getOriginalFilename();
                        String filePath = uploadDir + File.separator + fileName;
                        file.transferTo(new File(filePath));

                        // Build the URL for the uploaded image
                        String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                                        .path("/images/")
                                        .path(fileName)
                                        .toUriString();

                        // Update the profile image URL in the database for the user with the given
                        // email
                        updateUserProfileImage(email, fileUrl);

                        return fileUrl;
                } catch (IOException e) {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file");
                }
        }

        private void updateUserProfileImage(String email, String profileImageUrl) {
                Optional<User> userOptional = repository.findByEmail(email);
                if (userOptional.isPresent()) {
                        User user = userOptional.get();
                        user.setProfileImage(profileImageUrl);
                        repository.save(user);
                } else {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "User with email " + email + " not found");
                }
        }

        public AccountVerificationResponse accountVerification(AccountVerificationRequest request) {
                // Find the user by email
                Optional<User> userOptional = repository.findByEmail(request.getEmail());

                // Check if the user exists
                if (userOptional.isPresent()) {
                        User user = userOptional.get();

                        // Check if the verification code matches
                        if (user.getVerificationCode().equals(request.getCode())) {
                                // Code matches, user is verified
                                user.setVerified(true);
                                user.setVerificationCode(null); // Delete the verification code
                                repository.save(user);

                                return new AccountVerificationResponse("Account verified successfully");
                        } else {
                                // Code does not match
                                return new AccountVerificationResponse("Invalid verification code");
                        }
                } else {
                        // User not found
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                }
        }

        public PasswordResetResponse passwordReset(PasswordResetRequest request) {
                final String verificationCode = verificationCodeGeneratorService.generateRandomCode(6);

                final String message = "Dear " + request.getEmail() + ",\n" +
                        "You recently requested to reset your password for your Social Networking Site account. To complete this process, please use the following verification code:\n" +
                        "Verification Code: " + verificationCode + "\n" +
                        "Please enter this code in the provided field to confirm your password reset request.\n" +
                        "If you did not initiate this password reset, please disregard this message.\n" +
                        "Thank you for using Social Networking Site,\n" +
                        "Amalitech Team\n";

                // Retrieve the current user by email
                Optional<User> currentUserOptional = repository.findByEmail(request.getEmail());

                // Check if the user exists
                if (!currentUserOptional.isPresent()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A user with this email does not exist");
                }

                // Get the user from the Optional
                User currentUser = currentUserOptional.get();

                // Update the user's verification code
                currentUser.setVerificationCode(verificationCode);

                // Save the updated user
                repository.save(currentUser);

                // Send the password reset email
                mailService.sendEmail(request.getEmail(), "Social Networking Site Password Reset Verification Code", message);

                // Return a response indicating that the verification code has been sent
                return new PasswordResetResponse("Verification code sent successfully");
        }
        public ChangeResetPasswordResponse changeResetPassword(ChangeResetPasswordRequest request) {
                User currentUser = repository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "User does not exist"));

                if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid current password");
                }

                String newPasswordEncoded = passwordEncoder.encode(request.getNewPassword());
                currentUser.setPassword(newPasswordEncoded);
                repository.save(currentUser);

                return new ChangeResetPasswordResponse("Password changed successfully");
        }




}
