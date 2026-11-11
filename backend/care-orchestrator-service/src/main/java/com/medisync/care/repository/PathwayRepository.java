package com.medisync.care.repository;

import com.medisync.care.entity.Pathway;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PathwayRepository extends JpaRepository<Pathway, Long> {

}
