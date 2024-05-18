package com.eritten.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eritten.backend.models.Invitation;
import com.eritten.backend.models.InvitationStatus;
import com.eritten.backend.models.User;

import java.util.List;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    List<Invitation> findByReceiverAndStatus(User receiver, InvitationStatus status);

}
