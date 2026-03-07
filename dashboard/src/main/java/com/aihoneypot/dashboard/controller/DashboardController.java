package com.aihoneypot.dashboard.controller;

import com.aihoneypot.dashboard.dto.ThreatSessionDTO;
import com.aihoneypot.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API controller for the threat monitoring dashboard.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Threat monitoring and statistics API")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Get overall threat statistics.
     */
    @GetMapping("/stats")
    @Operation(summary = "Get threat statistics", description = "Returns aggregated statistics about detected threats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(dashboardService.getThreatStatistics());
    }

    /**
     * Get recent threat sessions.
     */
    @GetMapping("/threats/recent")
    @Operation(summary = "Get recent threats", description = "Returns the most recent threat detections")
    public ResponseEntity<List<ThreatSessionDTO>> getRecentThreats(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(dashboardService.getRecentThreats(limit));
    }

    /**
     * Get threats from the last N hours.
     */
    @GetMapping("/threats/last-hours")
    @Operation(summary = "Get threats in time range", description = "Returns threats detected in the last N hours")
    public ResponseEntity<List<ThreatSessionDTO>> getThreatsLastHours(
            @RequestParam(defaultValue = "24") int hours) {
        return ResponseEntity.ok(dashboardService.getRecentThreatsInHours(hours));
    }

    /**
     * Get threat count by client type.
     */
    @GetMapping("/stats/by-client-type")
    @Operation(summary = "Threats by client type", description = "Returns threat count grouped by client type")
    public ResponseEntity<Map<String, Long>> getThreatsByClientType() {
        return ResponseEntity.ok(dashboardService.getThreatCountByClientType());
    }

    /**
     * Get threat count by severity.
     */
    @GetMapping("/stats/by-severity")
    @Operation(summary = "Threats by severity", description = "Returns threat count grouped by severity level")
    public ResponseEntity<Map<String, Long>> getThreatsBySeverity() {
        return ResponseEntity.ok(dashboardService.getThreatCountBySeverity());
    }

    /**
     * Get top attacking IPs.
     */
    @GetMapping("/stats/top-ips")
    @Operation(summary = "Top attacking IPs", description = "Returns the IP addresses with the most threat detections")
    public ResponseEntity<List<Map<String, Object>>> getTopAttackingIPs(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getTopAttackingIPs(limit));
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns the health status of the dashboard service")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "AIHoneypot Dashboard",
            "timestamp", java.time.Instant.now().toString()
        ));
    }
}

