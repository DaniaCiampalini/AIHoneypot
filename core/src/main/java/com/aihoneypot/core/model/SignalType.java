package com.aihoneypot.core.model;

/**
 * Types of behavioral signals collected from HTTP requests.
 */
public enum SignalType {
    // Timing signals
    REQUEST_INTERVAL,
    SESSION_DURATION,

    // Header signals
    USER_AGENT_PATTERN,
    ACCEPT_HEADER_MISSING,
    ACCEPT_LANGUAGE_MISSING,
    HEADER_ORDER_ANOMALY,

    // Behavioral signals
    MOUSE_MOVEMENT_ABSENT,
    JAVASCRIPT_DISABLED,
    COOKIE_REJECTION,

    // Navigation signals
    DIRECT_ENDPOINT_ACCESS,
    NO_REFERER,
    SUSPICIOUS_URL_PATTERN,

    // Content signals
    FORM_SUBMISSION_SPEED,
    CANARY_TRAP_TRIGGERED,

    // Network signals
    IP_REPUTATION,
    GEOLOCATION_MISMATCH,

    // Statistical signals
    ANOMALY_SCORE,
    ENSEMBLE_PREDICTION
}

