package com.medisync.clinical;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.medisync.clinical", "com.medisync.common"})
public class ClinicalServiceApplication {
    public static void main(String[] args) { SpringApplication.run(ClinicalServiceApplication.class, args); }
}
