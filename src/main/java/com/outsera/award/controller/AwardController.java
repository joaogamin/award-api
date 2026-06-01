package com.outsera.award.controller;

import com.outsera.award.dto.AwardIntervalResponse;
import com.outsera.award.service.AwardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/awards")
public class AwardController {

    private final AwardService awardService;

    @Autowired
    public AwardController(AwardService awardService) {
        this.awardService = awardService;
    }

    @GetMapping("/intervals")
    public ResponseEntity<AwardIntervalResponse> getProducerIntervals() {
        AwardIntervalResponse response = awardService.calculateIntervals();
        if (response.getMin().isEmpty() && response.getMax().isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }
}