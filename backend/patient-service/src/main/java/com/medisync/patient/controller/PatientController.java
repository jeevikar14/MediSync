package com.medisync.patient.controller;

import com.medisync.patient.dto.PatientDtos.PatientRequest;
import com.medisync.patient.entity.Patient;
import com.medisync.patient.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Patient register(@Valid @RequestBody PatientRequest req,
                            @RequestHeader(value = "X-User-Name", defaultValue = "system") String actor) {
        return service.register(req, actor);
    }

    @GetMapping
    public List<Patient> list(@RequestParam(required = false) String q) { return service.search(q); }

    @GetMapping("/{id}")
    public Patient get(@PathVariable Long id) { return service.get(id); }

    @PutMapping("/{id}")
    public Patient update(@PathVariable Long id, @Valid @RequestBody PatientRequest req) { return service.update(id, req); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) { service.deactivate(id); }
}
