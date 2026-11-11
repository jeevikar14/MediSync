package com.medisync.patient.service;

import com.medisync.common.event.DomainEvent;
import com.medisync.common.event.EventTypes;
import com.medisync.common.event.Topics;
import com.medisync.common.exception.ResourceNotFoundException;
import com.medisync.common.kafka.DomainEventPublisher;
import com.medisync.patient.dto.PatientDtos.PatientRequest;
import com.medisync.patient.entity.Patient;
import com.medisync.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository repository;
    private final DomainEventPublisher publisher;

    @Transactional
    public Patient register(PatientRequest req, String actor) {
        Patient p = new Patient();
        apply(p, req);
        p.setMrn(String.format("MRN-%06d", repository.nextMrnNumber()));
        p.setStatus("ACTIVE");
        p = repository.save(p);

        publisher.publish(Topics.PATIENT_EVENTS, DomainEvent.of(EventTypes.PATIENT_REGISTERED, p.getId(), p.getId(), actor,
                "Patient registered: " + p.getFirstName() + " " + p.getLastName() + " (" + p.getMrn() + ")",
                Map.of("mrn", p.getMrn())));
        return p;
    }

    @Transactional(readOnly = true)
    public List<Patient> search(String q) {
        if (q == null || q.isBlank()) {
            return repository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        }
        return repository.search(q.trim());
    }

    @Transactional(readOnly = true)
    public Patient get(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Patient " + id + " not found"));
    }

    @Transactional
    public Patient update(Long id, PatientRequest req) {
        Patient p = get(id);
        apply(p, req);
        return repository.save(p);
    }

    /** Patients are never physically deleted (other services reference the id) - they are deactivated. */
    @Transactional
    public void deactivate(Long id) {
        Patient p = get(id);
        p.setStatus("INACTIVE");
        repository.save(p);
    }

    private void apply(Patient p, PatientRequest r) {
        p.setFirstName(r.firstName().trim());
        p.setLastName(r.lastName().trim());
        p.setDateOfBirth(r.dateOfBirth());
        p.setGender(r.gender());
        p.setPhone(r.phone());
        p.setEmail(r.email());
        p.setAddress(r.address());
        p.setBloodGroup(r.bloodGroup());
    }
}
