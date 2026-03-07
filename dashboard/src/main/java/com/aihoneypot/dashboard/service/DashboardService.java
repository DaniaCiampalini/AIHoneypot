package com.aihoneypot.dashboard.service;

import com.aihoneypot.analyzer.entity.ThreatSession;
import com.aihoneypot.analyzer.repository.ThreatSessionRepository;
import com.aihoneypot.core.model.ClientType;
import com.aihoneypot.core.model.Severity;
import com.aihoneypot.dashboard.dto.ThreatSessionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for dashboard data aggregation and statistics.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ThreatSessionRepository repository;

    /**
     * Get recent threat sessions.
     */
    public List<ThreatSessionDTO> getRecentThreats(int limit) {
        return repository.findTop100ByOrderByTimestampDesc()
            .stream()
            .limit(limit)
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get threat statistics summary.
     */
    public Map<String, Object> getThreatStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalThreats", repository.countByIsThreatTrue());
        stats.put("totalSessions", repository.count());
        stats.put("activeSessions", getActiveSessionCount());

        // Count by severity and client type
        stats.put("bySeverity", getThreatCountBySeverity());
        stats.put("byClientType", getThreatCountByClientType());

        // Canary trap hits
        stats.put("canaryTrapHits", repository.findByCanaryTrapTriggeredTrue().size());

        return stats;
    }

    /**
     * Get threats within a time range.
     */
    public List<ThreatSessionDTO> getThreatsInTimeRange(Instant start, Instant end) {
        return repository.findByTimestampBetween(start, end)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get threats from the last N hours.
     */
    public List<ThreatSessionDTO> getRecentThreatsInHours(int hours) {
        Instant start = Instant.now().minus(hours, ChronoUnit.HOURS);
        Instant end = Instant.now();
        return getThreatsInTimeRange(start, end);
    }

    /**
     * Get top attacking IP addresses.
     */
    public List<Map<String, Object>> getTopAttackingIPs(int limit) {
        List<Object[]> results = repository.findTopAttackingIps();
        return results.stream()
            .limit(limit)
            .map(row -> {
                Map<String, Object> map = new HashMap<>();
                map.put("ipAddress", row[0]);
                map.put("count", row[1]);
                return map;
            })
            .collect(Collectors.toList());
    }

    /**
     * Get threat count by client type.
     */
    public Map<String, Long> getThreatCountByClientType() {
        Map<String, Long> result = new HashMap<>();
        List<Object[]> data = repository.countByClientTypeGrouped();
        for (Object[] row : data) {
            ClientType type = (ClientType) row[0];
            Long count = (Long) row[1];
            result.put(type.name(), count);
        }
        return result;
    }

    /**
     * Get threat count by severity.
     */
    public Map<String, Long> getThreatCountBySeverity() {
        Map<String, Long> result = new HashMap<>();
        List<Object[]> data = repository.countBySeverity();
        for (Object[] row : data) {
            Severity severity = (Severity) row[0];
            Long count = (Long) row[1];
            result.put(severity.name(), count);
        }
        return result;
    }

    private int getActiveSessionCount() {
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        return repository.findByTimestampBetween(oneHourAgo, Instant.now()).size();
    }

    private ThreatSessionDTO toDTO(ThreatSession entity) {
        return ThreatSessionDTO.builder()
            .id(entity.getId())
            .sessionId(entity.getSessionId())
            .ipAddress(entity.getIpAddress())
            .timestamp(entity.getTimestamp())
            .clientType(entity.getClientType())
            .confidence(entity.getConfidence())
            .severity(entity.getSeverity())
            .isThreat(entity.getIsThreat())
            .userAgent(entity.getUserAgent())
            .firstUri(entity.getFirstUri())
            .requestCount(entity.getRequestCount())
            .explanation(entity.getExplanation())
            .canaryTrapTriggered(entity.getCanaryTrapTriggered())
            .build();
    }
}

