package com.aihoneypot.honeypot.controller;

import com.aihoneypot.analyzer.service.ThreatLogService;
import com.aihoneypot.collector.service.SessionStore;
import com.aihoneypot.core.interfaces.ThreatClassifier;
import com.aihoneypot.core.model.ClassificationResult;
import com.aihoneypot.core.model.RawRequestSignals;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Canary trap endpoints - URLs that legitimate users should never access.
 * Any access to these endpoints is highly suspicious and triggers immediate threat classification.
 *
 * Common trap patterns:
 * - Admin panels (/admin, /wp-admin)
 * - Config files (/.env, /config)
 * - Development endpoints (/api/internal)
 * - Version control (/.git)
 * - Backup files (/backup)
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class CanaryController {

    private final SessionStore sessionStore;
    private final ThreatClassifier threatClassifier;
    private final ThreatLogService threatLogService;

    /**
     * Admin panel trap
     */
    @RequestMapping(value = {"/admin", "/admin/**", "/wp-admin", "/wp-admin/**"},
                    method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, String>> adminTrap(HttpServletRequest request) {
        logCanaryHit(request, "admin_panel");
        return createDecoyResponse();
    }

    /**
     * Environment file trap
     */
    @GetMapping(value = {"/.env", "/.env.example", "/.env.local"})
    public ResponseEntity<Map<String, String>> envFileTrap(HttpServletRequest request) {
        logCanaryHit(request, "env_file");
        return createDecoyResponse();
    }

    /**
     * Config file trap
     */
    @GetMapping(value = {"/config", "/config/**", "/configuration"})
    public ResponseEntity<Map<String, String>> configTrap(HttpServletRequest request) {
        logCanaryHit(request, "config_file");
        return createDecoyResponse();
    }

    /**
     * Internal API trap
     */
    @RequestMapping(value = {"/api/internal", "/api/internal/**", "/internal/**"},
                    method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, String>> internalApiTrap(HttpServletRequest request) {
        logCanaryHit(request, "internal_api");
        return createDecoyResponse();
    }

    /**
     * Version control trap
     */
    @GetMapping(value = {"/.git", "/.git/**", "/.svn", "/.svn/**"})
    public ResponseEntity<Map<String, String>> vcsFileTrap(HttpServletRequest request) {
        logCanaryHit(request, "version_control");
        return createDecoyResponse();
    }

    /**
     * Backup file trap
     */
    @GetMapping(value = {"/backup", "/backup/**", "/backups/**", "/*.bak", "/*.backup"})
    public ResponseEntity<Map<String, String>> backupTrap(HttpServletRequest request) {
        logCanaryHit(request, "backup_file");
        return createDecoyResponse();
    }

    /**
     * Database dump trap
     */
    @GetMapping(value = {"/*.sql", "/dump.sql", "/backup.sql", "/database.sql"})
    public ResponseEntity<Map<String, String>> databaseDumpTrap(HttpServletRequest request) {
        logCanaryHit(request, "database_dump");
        return createDecoyResponse();
    }

    /**
     * PHPMyAdmin trap
     */
    @GetMapping(value = {"/phpmyadmin", "/phpmyadmin/**", "/pma/**"})
    public ResponseEntity<Map<String, String>> phpMyAdminTrap(HttpServletRequest request) {
        logCanaryHit(request, "phpmyadmin");
        return createDecoyResponse();
    }

    /**
     * Log the canary trap hit and perform threat classification
     */
    private void logCanaryHit(HttpServletRequest request, String trapType) {
        String sessionId = request.getSession().getId();
        String ipAddress = request.getRemoteAddr();
        String uri = request.getRequestURI();

        log.warn("⚠️  CANARY TRAP HIT - Type: {}, Session: {}, IP: {}, URI: {}, UA: {}",
            trapType, sessionId, ipAddress, uri, request.getHeader("User-Agent"));

        // Get signals from session store
        List<RawRequestSignals> sessionRequests = sessionStore.getSessionRequests(sessionId);
        if (!sessionRequests.isEmpty()) {
            RawRequestSignals latestSignals = sessionRequests.get(sessionRequests.size() - 1);

            // Classify as threat
            try {
                ClassificationResult result = threatClassifier.classify(latestSignals);

                // Log to database
                threatLogService.logThreat(result, latestSignals);

                log.info("Threat classified - Type: {}, Severity: {}, Confidence: {:.2f}",
                    result.getClientType(), result.getSeverity(), result.getConfidence());

            } catch (Exception e) {
                log.error("Error classifying canary trap hit: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Create a realistic decoy response to not tip off the attacker
     */
    private ResponseEntity<Map<String, String>> createDecoyResponse() {
        // Return 404 to appear as if the endpoint doesn't exist
        // This makes the honeypot stealthier
        return ResponseEntity.notFound().build();

        // Alternative: return a fake "Forbidden" response
        // return ResponseEntity.status(HttpStatus.FORBIDDEN)
        //     .body(Map.of("error", "Access denied"));
    }
}

