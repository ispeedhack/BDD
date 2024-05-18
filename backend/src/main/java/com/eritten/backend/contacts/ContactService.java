package com.eritten.backend.contacts;

import java.util.List;

import org.springframework.stereotype.Service;

import com.eritten.backend.models.Contact;
import com.eritten.backend.models.User;
import com.eritten.backend.repositories.UserRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class ContactService {
    private final UserRepository userRepository;

    public List<User> getAllUsers() {
        // Retrieve all users from the database
        return userRepository.findAll();
    }

    public List<Contact> getContactsByEmail(String email) {
        // Find the user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Return the contacts associated with the user
        return user.getContacts();
    }

    public ContactResponse addContact(ContactRequest request) {
        // Find the user by email
        User user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find the contact user by email
        User contactUser = userRepository.findByEmail(request.getContactEmail())
                .orElseThrow(() -> new RuntimeException("Contact user not found"));

        // Create a new contact
        Contact contact = new Contact();
        contact.setUser(user);
        contact.setContactUser(contactUser);

        // Add the contact to the user's contact list
        user.getContacts().add(contact);

        // Save the user to update the contact list
        userRepository.save(user);
        return new ContactResponse("Contact added successfully");
    }

    public ContactResponse removeContact(ContactRequest request) {
        // Find the user by email
        User user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find the contact user by email
        User contactUser = userRepository.findByEmail(request.getContactEmail())
                .orElseThrow(() -> new RuntimeException("Contact user not found"));

        // Find and remove the contact from the user's contact list
        user.getContacts().removeIf(contact -> contact.getContactUser().equals(contactUser));

        // Save the user to update the contact list
        userRepository.save(user);
        return new ContactResponse("Contact removed successfully");
    }
}