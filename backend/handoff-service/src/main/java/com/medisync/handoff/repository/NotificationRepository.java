package com.medisync.handoff.repository;

import com.medisync.handoff.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findTop100ByRecipientInOrderByCreatedAtDesc(Collection<String> recipients);
}
