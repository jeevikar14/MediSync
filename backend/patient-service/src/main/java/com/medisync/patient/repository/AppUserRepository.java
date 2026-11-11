package com.medisync.patient.repository;

import com.medisync.patient.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsernameAndActiveTrue(String username);
    List<AppUser> findByActiveTrueOrderByFullName();
    List<AppUser> findByRoleAndActiveTrueOrderByFullName(String role);
}
