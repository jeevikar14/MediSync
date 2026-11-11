package com.medisync.handoff.controller;

import com.medisync.handoff.entity.Notification;
import com.medisync.handoff.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private static final String USER = "X-User-Name";
    private static final String ROLE = "X-User-Role";
    private final NotificationService service;

    @GetMapping
    public List<Notification> mine(@RequestHeader(USER) String user, @RequestHeader(ROLE) String role) {
        return service.forUser(user, role);
    }

    @PostMapping("/{id}/read")
    public Notification read(@PathVariable Long id, @RequestHeader(USER) String user, @RequestHeader(ROLE) String role) {
        return service.markRead(id, user, role);
    }

    @PostMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void readAll(@RequestHeader(USER) String user, @RequestHeader(ROLE) String role) {
        service.markAllRead(user, role);
    }
}
