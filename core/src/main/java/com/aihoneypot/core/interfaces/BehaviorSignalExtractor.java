package com.aihoneypot.core.interfaces;

import com.aihoneypot.core.model.RawRequestSignals;

import java.util.Map;

/**
 * Interface for extracting behavioral signals from raw HTTP requests.
 * Implementations transform low-level request data into feature vectors
 * suitable for machine learning classification.
 */
public interface BehaviorSignalExtractor {

    /**
     * Extract behavioral features from raw request signals.
     *
     * @param rawSignals The raw HTTP request signals
     * @return A map of feature names to numerical values
     */
    Map<String, Double> extractFeatures(RawRequestSignals rawSignals);

    /**
     * Extract features from a session (multiple requests).
     *
     * @param sessionId The session identifier
     * @param rawSignalsList List of raw signals for the session
     * @return A map of aggregated feature names to values
     */
    Map<String, Double> extractSessionFeatures(String sessionId,
                                                java.util.List<RawRequestSignals> rawSignalsList);
}

