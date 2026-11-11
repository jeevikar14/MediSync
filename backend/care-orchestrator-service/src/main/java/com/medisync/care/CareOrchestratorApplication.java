package com.medisync.care;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.medisync.care", "com.medisync.common"})
public class CareOrchestratorApplication {
    public static void main(String[] args) { SpringApplication.run(CareOrchestratorApplication.class, args); }
}
