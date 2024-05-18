package com.eritten.backend.contacts;

import java.util.List;

import org.springframework.stereotype.Service;

import com.eritten.backend.models.Blacklist;
import com.eritten.backend.models.User;
import com.eritten.backend.repositories.BlacklistRepository;
import com.eritten.backend.repositories.UserRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class BlacklistService {
    private final BlacklistRepository blacklistRepository;
    private final UserRepository userRepository;

    public BlacklistResponse blockContact(BlacklistRequest request) {
        User user = getUserByEmail(request.getUserEmail());
        User blockedUser = getUserByEmail(request.getBlockedUserEmail());

        Blacklist blacklistEntry = new Blacklist();
        blacklistEntry.setUser(user);
        blacklistEntry.setBlockedUser(blockedUser);

        blacklistRepository.save(blacklistEntry);

        return new BlacklistResponse("Contact blocked successfully");
    }

    public BlacklistResponse unblockContact(BlacklistRequest request) {
        User user = getUserByEmail(request.getUserEmail());
        User blockedUser = getUserByEmail(request.getBlockedUserEmail());

        blacklistRepository.deleteByUserAndBlockedUser(user, blockedUser);

        return new BlacklistResponse("Contact unblocked successfully");
    }

    public List<Blacklist> getBlockedContacts(String userEmail) {
        User user = getUserByEmail(userEmail);
        return blacklistRepository.findByBlockedUser(user);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
