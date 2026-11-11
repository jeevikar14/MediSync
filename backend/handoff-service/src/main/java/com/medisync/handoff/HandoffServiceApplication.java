package com.medisync.handoff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.medisync.handoff", "com.medisync.common"})
public class HandoffServiceApplication {
    public static void main(String[] args) { SpringApplication.run(HandoffServiceApplication.class, args); }
}
