package com.aihoneypot.analyzer.entity;

import com.aihoneypot.core.model.ClientType;
import com.aihoneypot.core.model.Severity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * JPA entity representing a detected threat session.
 * Persisted to database for historical analysis and reporting.
 */
@Entity
@Table(name = "threat_sessions", indexes = {
    @Index(name = "idx_session_id", columnList = "sessionId"),
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_ip_address", columnList = "ipAddress"),
    @Index(name = "idx_severity", columnList = "severity")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Session identifier from the servlet session
     */
    @Column(nullable = false, unique = true)
    private String sessionId;

    /**
     * Source IP address
     */
    @Column(nullable = false)
    private String ipAddress;

    /**
     * Timestamp when the threat was detected
     */
    @Column(nullable = false)
    private Instant timestamp;

    /**
     * Detected client type
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClientType clientType;

    /**
     * Confidence score (0.0 to 1.0)
     */
    @Column(nullable = false)
    private Double confidence;

    /**
     * Threat severity
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    /**
     * Is this confirmed as a threat?
     */
    @Column(nullable = false)
    private Boolean isThreat;

    /**
     * Anomaly score from ML analysis
     */
    private Double anomalyScore;

    /**
     * User-Agent string
     */
    @Column(length = 1000)
    private String userAgent;

    /**
     * First URI accessed
     */
    @Column(length = 500)
    private String firstUri;

    /**
     * Total number of requests in this session
     */
    private Integer requestCount;

    /**
     * Human-readable explanation
     */
    @Column(length = 2000)
    private String explanation;

    /**
     * Name of the classifier that detected this
     */
    private String classifierName;

    /**
     * Whether a canary trap was triggered
     */
    private Boolean canaryTrapTriggered;
}

