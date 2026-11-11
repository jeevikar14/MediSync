package com.medisync.patient.config;

import com.medisync.common.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityBeans {

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    JwtUtil jwtUtil(@Value("${medisync.jwt.secret}") String secret,
                    @Value("${medisync.jwt.expiration-minutes}") long minutes) {
        return new JwtUtil(secret, minutes);
    }
}
