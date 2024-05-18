package com.eritten.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eritten.backend.models.Content;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {

}
