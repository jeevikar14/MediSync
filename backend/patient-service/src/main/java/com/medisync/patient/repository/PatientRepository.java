package com.medisync.patient.repository;

import com.medisync.patient.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    @Query("""
           select p from Patient p
           where lower(p.firstName) like lower(concat('%', :q, '%'))
              or lower(p.lastName)  like lower(concat('%', :q, '%'))
              or lower(p.mrn)       like lower(concat('%', :q, '%'))
           order by p.id desc
           """)
    List<Patient> search(@Param("q") String q);

    @Query(value = "select nextval('mrn_seq')", nativeQuery = true)
    Long nextMrnNumber();
}
