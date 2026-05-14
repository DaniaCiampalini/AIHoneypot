package com.aihoneypot.analyzer.classification;

import com.aihoneypot.core.interfaces.ThreatClassifier;
import com.aihoneypot.core.model.ClassificationResult;
import com.aihoneypot.core.model.RawRequestSignals;
import com.aihoneypot.core.model.Severity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Ensemble Classifier (Layer 3) that aggregates results from Rule-based and ML layers.
 * Implements logic to resolve discrepancies based on confidence and severity.
 * Essential for "No Free Lunch" discussion in the thesis.
 */
@Slf4j
@Primary
@Component("ensembleClassifier")
public class EnsembleClassifier implements ThreatClassifier {

    private final ThreatClassifier ruleBased;
    private final ThreatClassifier mlBased;

    public EnsembleClassifier(
            @Qualifier("ruleBasedClassifier") ThreatClassifier ruleBased,
            @Qualifier("isolationForestAnomalyDetector") ThreatClassifier mlBased) {
        this.ruleBased = ruleBased;
        this.mlBased = mlBased;
    }

    @Override
    public ClassificationResult classify(RawRequestSignals signals) {
        ClassificationResult ruleResult = ruleBased.classify(signals);
        ClassificationResult mlResult = mlBased.classify(signals);

        boolean isThreat;
        double confidence;
        Severity severity;
        String explanation;
        Map<String, Object> features = new HashMap<>();

        // Logic: If both agree, high confidence
        if (ruleResult.isThreat() == mlResult.isThreat()) {
            isThreat = ruleResult.isThreat();
            confidence = Math.max(ruleResult.getConfidence(), mlResult.getConfidence());
            severity = isThreat ? 
                    (ruleResult.getSeverity().ordinal() > mlResult.getSeverity().ordinal() ? 
                            ruleResult.getSeverity() : mlResult.getSeverity()) : 
                    Severity.LOW;
            explanation = isThreat ? 
                    "Ensemble consensus: both layers detected threat." : 
                    "Ensemble consensus: normal behavior.";
        } else {
            // Discrepancy detected
            log.info("📊 Ensemble discrepancy for session {}: Rule={} ML={}", 
                    signals.getSessionId(), ruleResult.isThreat(), mlResult.isThreat());
            
            // Heuristic: ML is more sensitive to unknown patterns, Rule is more reliable for known patterns
            if (ruleResult.isThreat()) {
                // Rule-based is usually high precision for known patterns
                isThreat = true;
                confidence = ruleResult.getConfidence();
                severity = ruleResult.getSeverity();
                explanation = "Threat detected by Layer 1 (Rule-based). Layer 2 (ML) did not confirm.";
            } else {
                // ML detected something the rules missed (Thesis focus: unknown anomalies)
                isThreat = true;
                confidence = mlResult.getConfidence() * 0.8; // Slightly reduced confidence for unconfirmed ML detections
                severity = mlResult.getSeverity();
                explanation = "Anomalous behavior detected by Layer 2 (Isolation Forest). Layer 1 missed it.";
            }
        }

        features.put("rule_threat", ruleResult.isThreat());
        features.put("ml_threat", mlResult.isThreat());
        features.put("rule_confidence", ruleResult.getConfidence());
        features.put("ml_confidence", mlResult.getConfidence());

        return ClassificationResult.builder()
                .sessionId(signals.getSessionId())
                .timestamp(Instant.now())
                .clientType(isThreat ? mlResult.getClientType() : ruleResult.getClientType())
                .confidence(confidence)
                .severity(severity)
                .isThreat(isThreat)
                .anomalyScore(mlResult.getAnomalyScore())
                .explanation(explanation)
                .triggeredFeatures(features)
                .classifierName("Ensemble")
                .build();
    }

    @Override
    public String getName() {
        return "Ensemble";
    }

    @Override
    public boolean isReady() {
        return ruleBased.isReady() && mlBased.isReady();
    }
}
