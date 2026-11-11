package com.medisync.patient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.medisync.patient", "com.medisync.common"})
public class PatientServiceApplication {
    public static void main(String[] args) { SpringApplication.run(PatientServiceApplication.class, args); }
}
