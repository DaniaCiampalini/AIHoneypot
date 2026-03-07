package com.aihoneypot.core.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Result of threat classification for a session or request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationResult {

    /**
     * Session identifier
     */
    private String sessionId;

    /**
     * Timestamp of classification
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;

    /**
     * Detected client type
     */
    private ClientType clientType;

    /**
     * Confidence score (0.0 to 1.0)
     */
    private double confidence;

    /**
     * Threat severity level
     */
    private Severity severity;

    /**
     * Whether this is a confirmed threat
     */
    private boolean isThreat;

    /**
     * Anomaly score from statistical analysis
     */
    private double anomalyScore;

    /**
     * Human-readable explanation of the classification
     */
    private String explanation;

    /**
     * Triggered rules or features that led to this classification
     */
    private Map<String, Object> triggeredFeatures;

    /**
     * Name of the classifier that produced this result
     */
    private String classifierName;
}

