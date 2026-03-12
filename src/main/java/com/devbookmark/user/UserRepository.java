package com.devbookmark.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsernameAndIdNot(String username, java.util.UUID id);
    long countByBannedTrue();
    org.springframework.data.domain.Page<User> findAllByOrderByCreatedAtDesc(
            org.springframework.data.domain.Pageable pageable
    );
}