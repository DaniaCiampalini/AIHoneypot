package com.aihoneypot.xdetector.controller;

import com.aihoneypot.xdetector.model.BotScore;
import com.aihoneypot.xdetector.model.ManualAccountInput;
import com.aihoneypot.xdetector.service.XBotDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API controller for X bot detection.
 */
@Slf4j
@RestController
@RequestMapping("/api/x-detector")
@RequiredArgsConstructor
@Tag(name = "X Bot Detector", description = "API for detecting bots and AI agents on X (Twitter)")
public class XDetectorController {

    private final XBotDetectionService detectionService;

    /**
     * Analyze an X account with manual input data.
     */
    @PostMapping("/analyze/manual")
    @Operation(
        summary = "Analyze X account (manual input)",
        description = "Analyze an X account using manually provided data. " +
                     "Use this when you don't have API access - just copy public profile data."
    )
    public ResponseEntity<BotScore> analyzeManual(@RequestBody ManualAccountInput input) {
        log.info("Manual analysis request for @{}", input.getUsername());

        try {
            BotScore result = detectionService.analyzeManual(input);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error analyzing account: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the X detector service is running")
    public ResponseEntity<java.util.Map<String, String>> health() {
        return ResponseEntity.ok(java.util.Map.of(
            "status", "UP",
            "service", "X Bot Detector",
            "timestamp", java.time.Instant.now().toString()
        ));
    }

    /**
     * Get analyzer information.
     */
    @GetMapping("/info")
    @Operation(summary = "Get analyzer info", description = "Returns information about the detection system")
    public ResponseEntity<java.util.Map<String, Object>> getInfo() {
        return ResponseEntity.ok(java.util.Map.of(
            "version", "1.0.0",
            "analyzers", java.util.List.of(
                "ProfileAnalyzer (20%)",
                "NetworkAnalyzer (25%)",
                "TemporalAnalyzer (25%)",
                "TextAnalyzer (30%)",
                "BehaviorAnalyzer (20%)"
            ),
            "signals", 31,
            "description", "Multi-signal bot detection system for X (Twitter)",
            "modes", java.util.List.of("manual_input", "api_integration")
        ));
    }
}

