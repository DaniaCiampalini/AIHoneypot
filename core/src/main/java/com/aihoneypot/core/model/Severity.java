package com.aihoneypot.core.model;

/**
 * Severity level for detected threats.
 */
public enum Severity {
    /**
     * Low severity - minimal threat
     */
    LOW,

    /**
     * Medium severity - suspicious activity
     */
    MEDIUM,

    /**
     * High severity - likely malicious
     */
    HIGH,

    /**
     * Critical severity - confirmed attack
     */
    CRITICAL
}

