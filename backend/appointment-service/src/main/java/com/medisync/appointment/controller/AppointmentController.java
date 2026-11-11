package com.medisync.appointment.controller;

import com.medisync.appointment.dto.AppointmentDtos.*;
import com.medisync.appointment.entity.Appointment;
import com.medisync.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private static final String USER = "X-User-Name";
    private final AppointmentService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Appointment book(@Valid @RequestBody BookRequest req, @RequestHeader(value = USER, defaultValue = "system") String actor) {
        return service.book(req, actor);
    }

    @GetMapping
    public List<Appointment> list(@RequestParam(required = false) Long patientId, @RequestParam(required = false) String status) {
        return service.list(patientId, status);
    }

    @GetMapping("/{id}")
    public Appointment get(@PathVariable Long id) { return service.get(id); }

    @PostMapping("/{id}/complete")
    public Appointment complete(@PathVariable Long id, @Valid @RequestBody(required = false) NotesRequest req,
                                @RequestHeader(value = USER, defaultValue = "system") String actor) {
        return service.complete(id, req == null ? null : req.notes(), actor);
    }

    @PostMapping("/{id}/missed")
    public Appointment missed(@PathVariable Long id, @Valid @RequestBody(required = false) NotesRequest req,
                              @RequestHeader(value = USER, defaultValue = "system") String actor) {
        return service.markMissed(id, req == null ? null : req.notes(), actor);
    }
}
