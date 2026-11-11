package com.medisync.care.repository;

import com.medisync.care.entity.TimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TimelineEventRepository extends JpaRepository<TimelineEvent, Long> {
    boolean existsByEventId(String eventId);
    List<TimelineEvent> findByPatientIdOrderByOccurredAtAsc(Long patientId);
    List<TimelineEvent> findByPatientIdAndEventType(Long patientId, String eventType);
}
