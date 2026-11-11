package com.medisync.clinical.controller;

import com.medisync.clinical.dto.ClinicalDtos.*;
import com.medisync.clinical.entity.ClinicalNote;
import com.medisync.clinical.entity.LabOrder;
import com.medisync.clinical.entity.LabReport;
import com.medisync.clinical.service.ClinicalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinical")
@RequiredArgsConstructor
public class ClinicalController {

    private static final String USER = "X-User-Name";
    private final ClinicalService service;

    @PostMapping("/lab-orders")
    @ResponseStatus(HttpStatus.CREATED)
    public LabOrder order(@Valid @RequestBody LabOrderRequest req, @RequestHeader(value = USER, defaultValue = "system") String actor) {
        return service.orderLab(req, actor);
    }

    @GetMapping("/lab-orders")
    public List<LabOrder> orders(@RequestParam(required = false) Long patientId) { return service.listOrders(patientId); }

    @PostMapping("/lab-reports")
    @ResponseStatus(HttpStatus.CREATED)
    public LabReport upload(@Valid @RequestBody LabReportRequest req, @RequestHeader(value = USER, defaultValue = "system") String actor) {
        return service.uploadReport(req, actor);
    }

    @GetMapping("/lab-reports")
    public List<LabReport> reports(@RequestParam(required = false) Long patientId, @RequestParam(required = false) Boolean reviewed) {
        return service.listReports(patientId, reviewed);
    }

    @PostMapping("/lab-reports/{id}/review")
    public LabReport review(@PathVariable Long id, @Valid @RequestBody(required = false) ReviewRequest req,
                            @RequestHeader(value = USER, defaultValue = "system") String actor) {
        return service.review(id, req == null ? null : req.comment(), actor);
    }

    @PostMapping("/notes")
    @ResponseStatus(HttpStatus.CREATED)
    public ClinicalNote addNote(@Valid @RequestBody NoteRequest req, @RequestHeader(value = USER, defaultValue = "system") String actor) {
        return service.addNote(req, actor);
    }

    @GetMapping("/notes")
    public List<ClinicalNote> notes(@RequestParam(required = false) Long patientId) { return service.listNotes(patientId); }
}
