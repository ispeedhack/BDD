package com.eritten.backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eritten.backend.models.Blacklist;
import com.eritten.backend.models.User;

@Repository
public interface BlacklistRepository extends JpaRepository<Blacklist, Long> {

    List<Blacklist> findByUser(User user);

    List<Blacklist> findByBlockedUser(User blockedUser);

    void deleteByUserAndBlockedUser(User user, User blockedUser);
}
