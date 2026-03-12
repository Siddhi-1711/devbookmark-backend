package com.devbookmark.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);

    long countByRecipientIdAndReadAtIsNull(UUID recipientId);

    long deleteByRecipientIdAndCreatedAtBefore(UUID recipientId, Instant before);
    @Modifying
    @Query("""
    update Notification n
    set n.readAt = :now
    where n.recipient.id = :me
      and n.readAt is null
""")
    int markAllRead(@Param("me") UUID me, @Param("now") Instant now);

    long deleteByIdAndRecipientId(UUID id, UUID recipientId);
}