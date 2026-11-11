package com.medisync.appointment.service;

import com.medisync.appointment.dto.AppointmentDtos.BookRequest;
import com.medisync.appointment.entity.Appointment;
import com.medisync.appointment.repository.AppointmentRepository;
import com.medisync.common.event.DomainEvent;
import com.medisync.common.event.EventTypes;
import com.medisync.common.event.Topics;
import com.medisync.common.exception.BusinessRuleException;
import com.medisync.common.exception.ResourceNotFoundException;
import com.medisync.common.kafka.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository repository;
    private final DomainEventPublisher publisher;

    @Transactional
    public Appointment book(BookRequest req, String actor) {
        Appointment a = repository.save(Appointment.builder()
                .patientId(req.patientId()).doctorName(req.doctorName()).department(req.department())
                .scheduledAt(req.scheduledAt()).reason(req.reason()).followUp(req.followUp())
                .status("BOOKED").createdBy(actor).build());
        publish(EventTypes.APPOINTMENT_BOOKED, a, actor,
                (a.isFollowUp() ? "Follow-up appointment" : "Appointment") + " booked with " + a.getDoctorName()
                        + " (" + a.getDepartment() + ")");
        return a;
    }

    @Transactional
    public Appointment complete(Long id, String notes, String actor) {
        Appointment a = openAppointment(id);
        a.setStatus("COMPLETED");
        a.setNotes(notes);
        a = repository.save(a);
        publish(EventTypes.APPOINTMENT_COMPLETED, a, actor, "Appointment completed with " + a.getDoctorName());
        return a;
    }

    @Transactional
    public Appointment markMissed(Long id, String notes, String actor) {
        Appointment a = openAppointment(id);
        a.setStatus("MISSED");
        a.setNotes(notes);
        a = repository.save(a);
        publish(EventTypes.APPOINTMENT_MISSED, a, actor,
                "Patient MISSED " + (a.isFollowUp() ? "follow-up " : "") + "appointment with " + a.getDoctorName());
        return a;
    }

    @Transactional(readOnly = true)
    public Appointment get(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Appointment " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<Appointment> list(Long patientId, String status) {
        List<Appointment> all = patientId == null ? repository.findAllByOrderByScheduledAtDesc()
                : repository.findByPatientIdOrderByScheduledAtDesc(patientId);
        return (status == null || status.isBlank()) ? all
                : all.stream().filter(a -> a.getStatus().equalsIgnoreCase(status)).toList();
    }

    private Appointment openAppointment(Long id) {
        Appointment a = get(id);
        if (!"BOOKED".equals(a.getStatus())) {
            throw new BusinessRuleException("Appointment " + id + " is already " + a.getStatus());
        }
        return a;
    }

    private void publish(String type, Appointment a, String actor, String summary) {
        publisher.publish(Topics.APPOINTMENT_EVENTS, DomainEvent.of(type, a.getPatientId(), a.getId(), actor, summary,
                Map.of("doctor", a.getDoctorName(), "department", a.getDepartment(),
                        "followUp", String.valueOf(a.isFollowUp()), "scheduledAt", a.getScheduledAt().toString())));
    }
}
