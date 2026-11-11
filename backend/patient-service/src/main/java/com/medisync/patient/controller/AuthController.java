package com.medisync.patient.controller;

import com.medisync.common.exception.UnauthorizedException;
import com.medisync.common.security.JwtUtil;
import com.medisync.patient.dto.PatientDtos.*;
import com.medisync.patient.entity.AppUser;
import com.medisync.patient.repository.AppUserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Login lives in patient-service (it owns the user table). The gateway validates the issued tokens. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        AppUser user = users.findByUsernameAndActiveTrue(req.username())
                .filter(u -> encoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));
        String token = jwt.generate(user.getUsername(), user.getRole(), user.getFullName());
        return new LoginResponse(token, user.getUsername(), user.getFullName(), user.getRole());
    }

    /** Used by the UI to pick the receiving doctor of a handoff. */
    @GetMapping("/users")
    public List<UserSummary> users(@RequestParam(required = false) String role) {
        List<AppUser> list = (role == null || role.isBlank())
                ? users.findByActiveTrueOrderByFullName()
                : users.findByRoleAndActiveTrueOrderByFullName(role.toUpperCase());
        return list.stream().map(u -> new UserSummary(u.getUsername(), u.getFullName(), u.getRole())).toList();
    }
}
