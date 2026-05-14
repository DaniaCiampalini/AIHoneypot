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
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Threat monitoring and statistics API")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Root endpoint - health check.
     */
    @GetMapping("/")
    @Operation(summary = "Dashboard root", description = "Root endpoint for dashboard health check")
    public ResponseEntity<Map<String, String>> root() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "AIHoneypot Dashboard"
        ));
    }

    /**
     * Get overall threat statistics.
     */
    @GetMapping("/api/dashboard/stats")
    @Operation(summary = "Get threat statistics", description = "Returns aggregated statistics about detected threats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(dashboardService.getThreatStatistics());
    }

    /**
     * Get recent threat sessions.
     */
    @GetMapping("/api/dashboard/threats/recent")
    @Operation(summary = "Get recent threats", description = "Returns the most recent threat detections")
    public ResponseEntity<List<ThreatSessionDTO>> getRecentThreats(
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(dashboardService.getRecentThreats(limit));
    }

    /**
     * Get threats from the last N hours.
     */
    @GetMapping("/api/dashboard/threats/last-hours")
    @Operation(summary = "Get threats in time range", description = "Returns threats detected in the last N hours")
    public ResponseEntity<List<ThreatSessionDTO>> getThreatsLastHours(
            @RequestParam(defaultValue = "24") int hours) {
        return ResponseEntity.ok(dashboardService.getRecentThreatsInHours(hours));
    }

    /**
     * Get threat count by client type.
     */
    @GetMapping("/api/dashboard/stats/by-client-type")
    @Operation(summary = "Threats by client type", description = "Returns threat count grouped by client type")
    public ResponseEntity<Map<String, Long>> getThreatsByClientType() {
        return ResponseEntity.ok(dashboardService.getThreatCountByClientType());
    }

    /**
     * Get threat count by severity.
     */
    @GetMapping("/api/dashboard/stats/by-severity")
    @Operation(summary = "Threats by severity", description = "Returns threat count grouped by severity level")
    public ResponseEntity<Map<String, Long>> getThreatsBySeverity() {
        return ResponseEntity.ok(dashboardService.getThreatCountBySeverity());
    }

    /**
     * Get top attacking IPs.
     */
    @GetMapping("/api/dashboard/stats/top-ips")
    @Operation(summary = "Top attacking IPs", description = "Returns the IP addresses with the most threat detections")
    public ResponseEntity<List<Map<String, Object>>> getTopAttackingIPs(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getTopAttackingIPs(limit));
    }

    /**
     * Export threat data as CSV for thesis evaluation.
     */
    @GetMapping("/api/dashboard/export/csv")
    @Operation(summary = "Export threats to CSV", description = "Exports all threat data in CSV format for experimental analysis")
    public ResponseEntity<String> exportToCSV() {
        StringBuilder csv = new StringBuilder();
        csv.append("id,sessionId,ipAddress,timestamp,clientType,confidence,isThreat,ruleBased,mlBased,discrepancy,explanation\n");
        
        List<ThreatSessionDTO> threats = dashboardService.getRecentThreats(1000);
        for (ThreatSessionDTO t : threats) {
            csv.append(String.format("%d,%s,%s,%s,%s,%.2f,%b,%b,%b,%b,\"%s\"\n",
                t.getId(), t.getSessionId(), t.getIpAddress(), t.getTimestamp(),
                t.getClientType(), t.getConfidence(), t.getIsThreat(),
                t.getRuleBasedThreat(), t.getMlThreat(), t.getDiscrepancy(),
                t.getExplanation().replace("\"", "'")));
        }
        
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv")
                .header("Content-Disposition", "attachment; filename=threat_experiments.csv")
                .body(csv.toString());
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/api/dashboard/health")
    @Operation(summary = "Health check", description = "Returns the health status of the dashboard service")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "AIHoneypot Dashboard",
                "timestamp", java.time.Instant.now().toString()
        ));
    }
}
