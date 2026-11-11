package com.medisync.care.controller;

import com.medisync.care.dto.CareDtos.*;
import com.medisync.care.entity.CareGap;
import com.medisync.care.entity.TimelineEvent;
import com.medisync.care.repository.TimelineEventRepository;
import com.medisync.care.service.CareGapService;
import com.medisync.care.service.EpisodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/care")
@RequiredArgsConstructor
public class CareController {

    private static final String USER = "X-User-Name";
    private final CareGapService gapService;
    private final EpisodeService episodeService;
    private final TimelineEventRepository timeline;

    /** Patient Journey Timeline - chronological (oldest first). */
    @GetMapping("/timeline/{patientId}")
    public List<TimelineEvent> timeline(@PathVariable Long patientId) {
        return timeline.findByPatientIdOrderByOccurredAtAsc(patientId);
    }

    @GetMapping("/gaps")
    public List<CareGap> gaps(@RequestParam(required = false) Long patientId, @RequestParam(required = false) String status) {
        return gapService.list(patientId, status);
    }

    @GetMapping("/pathways")
    public List<PathwayView> pathways() { return episodeService.listPathways(); }

    @PostMapping("/episodes")
    @ResponseStatus(HttpStatus.CREATED)
    public EpisodeView start(@Valid @RequestBody StartEpisodeRequest req, @RequestHeader(value = USER, defaultValue = "system") String actor) {
        return episodeService.start(req.patientId(), req.pathwayId(), actor);
    }

    @GetMapping("/episodes")
    public List<EpisodeView> episodes(@RequestParam(required = false) Long patientId) { return episodeService.list(patientId); }

    @GetMapping("/episodes/{id}")
    public EpisodeView episode(@PathVariable Long id) { return episodeService.get(id); }

    @PostMapping("/episodes/steps/{stepId}/complete")
    public EpisodeView completeStep(@PathVariable Long stepId, @RequestHeader(value = USER, defaultValue = "system") String actor) {
        return episodeService.completeStepManually(stepId, actor);
    }

    @GetMapping("/episodes/{id}/verify")
    public VerificationResult verify(@PathVariable Long id) { return episodeService.verify(id); }

    /** 200 + episode when closed; 409 + verification report when continuity checks fail. */
    @PostMapping("/episodes/{id}/close")
    public ResponseEntity<?> close(@PathVariable Long id, @Valid @RequestBody(required = false) CloseEpisodeRequest req,
                                   @RequestHeader(value = USER, defaultValue = "system") String actor) {
        VerificationResult result = episodeService.close(id, req == null ? null : req.notes(), actor);
        if (result.verified()) return ResponseEntity.ok(episodeService.get(id));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
    }
}
