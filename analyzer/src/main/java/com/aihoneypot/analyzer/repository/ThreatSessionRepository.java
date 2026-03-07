package com.aihoneypot.analyzer.repository;

import com.aihoneypot.analyzer.entity.ThreatSession;
import com.aihoneypot.core.model.ClientType;
import com.aihoneypot.core.model.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ThreatSession entities.
 */
@Repository
public interface ThreatSessionRepository extends JpaRepository<ThreatSession, Long> {

    /**
     * Find a threat session by session ID.
     */
    Optional<ThreatSession> findBySessionId(String sessionId);

    /**
     * Find all threats from a specific IP address.
     */
    List<ThreatSession> findByIpAddress(String ipAddress);

    /**
     * Find all threats by client type.
     */
    List<ThreatSession> findByClientType(ClientType clientType);

    /**
     * Find all threats by severity.
     */
    List<ThreatSession> findBySeverity(Severity severity);

    /**
     * Find all confirmed threats.
     */
    List<ThreatSession> findByIsThreatTrue();

    /**
     * Find threats within a time range.
     */
    List<ThreatSession> findByTimestampBetween(Instant start, Instant end);

    /**
     * Find threats that triggered canary traps.
     */
    List<ThreatSession> findByCanaryTrapTriggeredTrue();

    /**
     * Count threats by client type.
     */
    long countByClientType(ClientType clientType);

    /**
     * Count total confirmed threats.
     */
    long countByIsThreatTrue();

    /**
     * Get recent threats (last N).
     */
    List<ThreatSession> findTop100ByOrderByTimestampDesc();

    /**
     * Get threat count by severity.
     */
    @Query("SELECT t.severity, COUNT(t) FROM ThreatSession t GROUP BY t.severity")
    List<Object[]> countBySeverity();

    /**
     * Get threat count by client type.
     */
    @Query("SELECT t.clientType, COUNT(t) FROM ThreatSession t GROUP BY t.clientType")
    List<Object[]> countByClientTypeGrouped();

    /**
     * Get top attacking IPs.
     */
    @Query("SELECT t.ipAddress, COUNT(t) FROM ThreatSession t WHERE t.isThreat = true GROUP BY t.ipAddress ORDER BY COUNT(t) DESC")
    List<Object[]> findTopAttackingIps();
}

