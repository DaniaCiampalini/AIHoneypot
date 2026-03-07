package com.aihoneypot.core.interfaces;

import com.aihoneypot.core.model.ClassificationResult;
import com.aihoneypot.core.model.RawRequestSignals;

/**
 * Interface for threat classification algorithms.
 * Implementations use various techniques (rule-based, ML, ensemble)
 * to classify client behavior as human, bot, AI agent, etc.
 */
public interface ThreatClassifier {

    /**
     * Classify a single request based on raw signals.
     *
     * @param signals The raw request signals to classify
     * @return Classification result with client type, confidence, and severity
     * @throws com.aihoneypot.core.exception.ClassificationException if classification fails
     */
    ClassificationResult classify(RawRequestSignals signals);

    /**
     * Get the name of this classifier.
     *
     * @return Classifier name (e.g., "RuleBased", "IsolationForest", "Ensemble")
     */
    String getName();

    /**
     * Check if this classifier is ready to use.
     *
     * @return true if the classifier is initialized and ready
     */
    boolean isReady();
}

