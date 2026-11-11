package com.medisync.clinical.service;

import com.medisync.clinical.dto.ClinicalDtos.*;
import com.medisync.clinical.entity.ClinicalNote;
import com.medisync.clinical.entity.LabOrder;
import com.medisync.clinical.entity.LabReport;
import com.medisync.clinical.repository.ClinicalNoteRepository;
import com.medisync.clinical.repository.LabOrderRepository;
import com.medisync.clinical.repository.LabReportRepository;
import com.medisync.common.event.DomainEvent;
import com.medisync.common.event.EventTypes;
import com.medisync.common.event.Topics;
import com.medisync.common.exception.BusinessRuleException;
import com.medisync.common.exception.ResourceNotFoundException;
import com.medisync.common.kafka.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClinicalService {

    private final LabOrderRepository orders;
    private final LabReportRepository reports;
    private final ClinicalNoteRepository notes;
    private final DomainEventPublisher publisher;

    // ---------------- lab orders ----------------
    @Transactional
    public LabOrder orderLab(LabOrderRequest req, String actor) {
        LabOrder order = orders.save(LabOrder.builder()
                .patientId(req.patientId()).testName(req.testName())
                .priority(req.priority() == null || req.priority().isBlank() ? "ROUTINE" : req.priority())
                .orderedBy(actor).status("ORDERED").notes(req.notes()).build());
        publisher.publish(Topics.CLINICAL_EVENTS, DomainEvent.of(EventTypes.LAB_ORDERED, order.getPatientId(), order.getId(), actor,
                "Lab test ordered: " + order.getTestName() + " by " + actor,
                Map.of("testName", order.getTestName(), "orderedBy", actor)));
        return order;
    }

    @Transactional(readOnly = true)
    public List<LabOrder> listOrders(Long patientId) {
        return patientId == null ? orders.findAllByOrderByOrderedAtDesc() : orders.findByPatientIdOrderByOrderedAtDesc(patientId);
    }

    // ---------------- lab reports ----------------
    @Transactional
    public LabReport uploadReport(LabReportRequest req, String actor) {
        LabOrder order = orders.findById(req.labOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Lab order " + req.labOrderId() + " not found"));
        if (!"ORDERED".equals(order.getStatus())) {
            throw new BusinessRuleException("Lab order " + order.getId() + " already has a report (status " + order.getStatus() + ")");
        }
        LabReport report = reports.save(LabReport.builder()
                .labOrderId(order.getId()).patientId(order.getPatientId()).testName(order.getTestName())
                .resultSummary(req.resultSummary()).abnormal(req.abnormal()).uploadedBy(actor).reviewed(false).build());
        order.setStatus("RESULT_UPLOADED");
        orders.save(order);

        publisher.publish(Topics.CLINICAL_EVENTS, DomainEvent.of(EventTypes.LAB_REPORT_UPLOADED, report.getPatientId(), report.getId(), actor,
                "Lab report uploaded: " + report.getTestName() + (report.isAbnormal() ? " (ABNORMAL)" : ""),
                Map.of("testName", report.getTestName(), "abnormal", String.valueOf(report.isAbnormal()),
                        "orderedBy", order.getOrderedBy(), "labOrderId", String.valueOf(order.getId()))));
        return report;
    }

    @Transactional(readOnly = true)
    public List<LabReport> listReports(Long patientId, Boolean reviewed) {
        List<LabReport> all = patientId == null ? reports.findAllByOrderByUploadedAtDesc()
                : reports.findByPatientIdOrderByUploadedAtDesc(patientId);
        return reviewed == null ? all : all.stream().filter(r -> r.isReviewed() == reviewed).toList();
    }

    @Transactional
    public LabReport review(Long reportId, String comment, String actor) {
        LabReport report = reports.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab report " + reportId + " not found"));
        if (report.isReviewed()) {
            throw new BusinessRuleException("Lab report " + reportId + " is already reviewed");
        }
        report.setReviewed(true);
        report.setReviewedBy(actor);
        report.setReviewedAt(Instant.now());
        report.setReviewComment(comment);
        report = reports.save(report);
        orders.findById(report.getLabOrderId()).ifPresent(o -> { o.setStatus("REVIEWED"); orders.save(o); });

        publisher.publish(Topics.CLINICAL_EVENTS, DomainEvent.of(EventTypes.LAB_REPORT_REVIEWED, report.getPatientId(), report.getId(), actor,
                "Lab report reviewed: " + report.getTestName() + " by " + actor,
                Map.of("testName", report.getTestName())));
        return report;
    }

    // ---------------- clinical notes ----------------
    @Transactional
    public ClinicalNote addNote(NoteRequest req, String actor) {
        ClinicalNote note = notes.save(ClinicalNote.builder().patientId(req.patientId())
                .noteType(req.noteType().toUpperCase()).content(req.content()).author(actor).build());
        publisher.publish(Topics.CLINICAL_EVENTS, DomainEvent.of(EventTypes.CLINICAL_NOTE_ADDED, note.getPatientId(), note.getId(), actor,
                "Clinical note (" + note.getNoteType() + ") added by " + actor,
                Map.of("noteType", note.getNoteType())));
        return note;
    }

    @Transactional(readOnly = true)
    public List<ClinicalNote> listNotes(Long patientId) {
        return patientId == null ? notes.findAllByOrderByCreatedAtDesc() : notes.findByPatientIdOrderByCreatedAtDesc(patientId);
    }
}
