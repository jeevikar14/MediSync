package com.medisync.appointment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.medisync.appointment", "com.medisync.common"})
public class AppointmentServiceApplication {
    public static void main(String[] args) { SpringApplication.run(AppointmentServiceApplication.class, args); }
}
