package com.outsera.award.controller;

import com.outsera.award.dto.AwardIntervalResponse;
import com.outsera.award.service.AwardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/awards")
public class AwardController {

    private static final Logger log = LoggerFactory.getLogger(AwardController.class);

    private final AwardService awardService;

    public AwardController(AwardService awardService) {
        this.awardService = awardService;
    }

    @GetMapping("/intervals")
    public ResponseEntity<AwardIntervalResponse> getProducerIntervals() {
        log.debug("GET /api/awards/intervals — calculating producer intervals");
        AwardIntervalResponse response = awardService.calculateIntervals();
        if (response.getMin().isEmpty() && response.getMax().isEmpty()) {
            log.debug("No interval data found — returning 204");
            return ResponseEntity.noContent().build();
        }
        log.debug("Returning intervals — min: {}, max: {}", response.getMin().size(), response.getMax().size());
        return ResponseEntity.ok(response);
    }
}