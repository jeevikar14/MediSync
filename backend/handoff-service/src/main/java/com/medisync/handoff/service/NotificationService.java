package com.medisync.handoff.service;

import com.medisync.common.exception.ForbiddenException;
import com.medisync.common.exception.ResourceNotFoundException;
import com.medisync.handoff.entity.Notification;
import com.medisync.handoff.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    @Transactional
    public void notify(String recipient, String type, String message, Long patientId) {
        if (recipient == null || recipient.isBlank()) return;
        repository.save(Notification.builder().recipient(recipient).type(type).message(message).patientId(patientId).build());
    }

    /** Notifications addressed to the user directly or broadcast to the user's role. */
    @Transactional(readOnly = true)
    public List<Notification> forUser(String username, String role) {
        return repository.findTop100ByRecipientInOrderByCreatedAtDesc(List.of(username, "ROLE:" + role));
    }

    @Transactional
    public Notification markRead(Long id, String username, String role) {
        Notification n = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Notification " + id + " not found"));
        if (!n.getRecipient().equals(username) && !n.getRecipient().equals("ROLE:" + role)) {
            throw new ForbiddenException("Not your notification");
        }
        n.setRead(true);
        return repository.save(n);
    }

    @Transactional
    public void markAllRead(String username, String role) {
        List<Notification> list = forUser(username, role);
        list.forEach(n -> n.setRead(true));
        repository.saveAll(list);
    }
}
