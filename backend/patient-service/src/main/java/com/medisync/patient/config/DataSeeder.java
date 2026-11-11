package com.medisync.patient.config;

import com.medisync.patient.entity.AppUser;
import com.medisync.patient.entity.Patient;
import com.medisync.patient.repository.AppUserRepository;
import com.medisync.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/** Seeds demo users and SYNTHETIC patients on first start. Password for all users: Password@123 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AppUserRepository users;
    private final PatientRepository patients;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (users.count() == 0) {
            String hash = encoder.encode("Password@123");
            user("admin", "System Admin", "ADMIN", hash);
            user("dr.smith", "Dr. Alice Smith", "DOCTOR", hash);
            user("dr.jones", "Dr. Bob Jones", "DOCTOR", hash);
            user("nurse.kim", "Nurse Kim Lee", "NURSE", hash);
            user("lab.tech", "Lab Tech Raj Patel", "LAB_TECHNICIAN", hash);
            user("reception", "Riya Reception", "RECEPTIONIST", hash);
            log.info("Seeded demo users");
        }
        if (patients.count() == 0) {
            patient("Aarav", "Sharma", LocalDate.of(1985, 3, 14), "MALE", "9000000001", "O+");
            patient("Meera", "Iyer", LocalDate.of(1972, 11, 2), "FEMALE", "9000000002", "A+");
            patient("Rohan", "Das", LocalDate.of(1990, 7, 21), "MALE", "9000000003", "B+");
            patient("Sana", "Khan", LocalDate.of(1965, 1, 30), "FEMALE", "9000000004", "AB-");
            patient("Vikram", "Rao", LocalDate.of(2001, 9, 9), "MALE", "9000000005", "O-");
            log.info("Seeded synthetic patients");
        }
    }

    private void user(String username, String name, String role, String hash) {
        users.save(AppUser.builder().username(username).fullName(name).role(role).passwordHash(hash).active(true).build());
    }

    private void patient(String first, String last, LocalDate dob, String gender, String phone, String blood) {
        Patient p = Patient.builder().firstName(first).lastName(last).dateOfBirth(dob).gender(gender)
                .phone(phone).bloodGroup(blood).email(first.toLowerCase() + "@example.test")
                .address("Synthetic address, Bengaluru").status("ACTIVE")
                .mrn(String.format("MRN-%06d", patients.nextMrnNumber())).build();
        patients.save(p);
    }
}
