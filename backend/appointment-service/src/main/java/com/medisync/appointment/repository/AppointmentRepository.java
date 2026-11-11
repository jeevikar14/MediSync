package com.medisync.appointment.repository;

import com.medisync.appointment.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findAllByOrderByScheduledAtDesc();
    List<Appointment> findByPatientIdOrderByScheduledAtDesc(Long patientId);
}
