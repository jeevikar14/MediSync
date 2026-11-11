package com.medisync.handoff.controller;

import com.medisync.handoff.dto.HandoffDtos.*;
import com.medisync.handoff.entity.KnownGap;
import com.medisync.handoff.service.HandoffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/handoffs")
@RequiredArgsConstructor
public class HandoffController {

    private static final String USER = "X-User-Name";
    private static final String ROLE = "X-User-Role";
    private final HandoffService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HandoffView create(@Valid @RequestBody HandoffRequest req, @RequestHeader(value = USER, defaultValue = "system") String actor) {
        return service.create(req, actor);
    }

    @GetMapping
    public List<HandoffView> list(@RequestParam(required = false) Long patientId, @RequestParam(required = false) String status,
                                  @RequestParam(required = false) String toDoctor) {
        return service.list(patientId, status, toDoctor);
    }

    @GetMapping("/{id}")
    public HandoffView get(@PathVariable Long id) { return service.get(id); }

    @PostMapping("/{id}/acknowledge")
    public HandoffView acknowledge(@PathVariable Long id, @Valid @RequestBody(required = false) AcknowledgeRequest req,
                                   @RequestHeader(value = USER, defaultValue = "system") String actor,
                                   @RequestHeader(value = ROLE, defaultValue = "") String role) {
        return service.acknowledge(id, req == null ? null : req.comment(), actor, role);
    }

    /** Open care gaps for a patient - proposed as pending items when composing a handoff. */
    @GetMapping("/suggested-items")
    public List<KnownGap> suggested(@RequestParam Long patientId) { return service.suggestedGaps(patientId); }
}
