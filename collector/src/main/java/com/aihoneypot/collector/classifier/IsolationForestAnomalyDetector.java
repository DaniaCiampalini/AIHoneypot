package com.aihoneypot.collector.classifier;

import com.aihoneypot.core.interfaces.ThreatClassifier;
import com.aihoneypot.core.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import weka.classifiers.misc.IsolationForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Anomaly detector basato su Isolation Forest (Weka).
 * Addestrato su traffico normale per rilevare deviazioni.
 * Implementa l'interfaccia ThreatClassifier per integrazione nel sistema.
 */
@Slf4j
@Component("isolationForestAnomalyDetector")
@RequiredArgsConstructor
public class IsolationForestAnomalyDetector implements ThreatClassifier {

    private static final double ANOMALY_THRESHOLD = -0.1; // Soglia per anomaly score (valori più negativi = più anomali)
    private static final String CLASSIFIER_NAME = "IsolationForest";

    private IsolationForest isolationForest;
    private Instances trainingStructure;

    @Override
    public ClassificationResult classify(RawRequestSignals signals) {
        double anomalyScore = computeAnomalyScore(signals);
        boolean isAnomalous = anomalyScore < ANOMALY_THRESHOLD;

        // Mappa lo score di Weka ([-1, 1] dopo normalizzazione) a un valore di confidenza
        // IF score vicino a -1 -> alta confidenza di anomalia
        double confidence = Math.min(Math.abs(anomalyScore + 1.0) / 2.0, 1.0);
        
        Severity severity = isAnomalous ? Severity.HIGH : Severity.LOW;
        
        return ClassificationResult.builder()
                .sessionId(signals.getSessionId())
                .timestamp(Instant.now())
                .clientType(isAnomalous ? ClientType.UNKNOWN : ClientType.HUMAN_BROWSER)
                .confidence(confidence)
                .severity(severity)
                .isThreat(isAnomalous)
                .anomalyScore(anomalyScore)
                .explanation(isAnomalous ? "Behavior detected as anomalous by Isolation Forest" : "Normal behavior detected")
                .classifierName(CLASSIFIER_NAME)
                .triggeredFeatures(new HashMap<>()) // Feature specifiche potrebbero essere aggiunte qui
                .build();
    }

    @Override
    public String getName() {
        return CLASSIFIER_NAME;
    }

    @Override
    public boolean isReady() {
        return isolationForest != null && trainingStructure != null;
    }

    @PostConstruct
    public void init() {
        try {
            log.info("Initializing Isolation Forest...");
            isolationForest = new IsolationForest();
            isolationForest.setNumTrees(100);
            isolationForest.setSubsampleSize(256);
            isolationForest.setSeed(42);

            // Definisci le feature
            trainingStructure = createFeatureStructure();
            log.info("Isolation Forest initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Isolation Forest: {}", e.getMessage(), e);
        }
    }

    /**
     * Valuta il request come potenzialmente anomalo.
     */
    public boolean isAnomalous(RawRequestSignals signals) {
        try {
            double anomalyScore = computeAnomalyScore(signals);
            boolean isAnomalous = anomalyScore < ANOMALY_THRESHOLD; // Più negativo = più anomalo

            if (isAnomalous) {
                log.debug("🚨 ANOMALY DETECTED - Session: {} - Score: {:.3f} - URI: {}",
                        signals.getSessionId(), anomalyScore, signals.getUri());
            }

            return isAnomalous;
        } catch (Exception e) {
            log.error("Error in anomaly detection: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Calcola lo score di anomalia usando Isolation Forest.
     * @return score negativo per anomalie, positivo per normale
     */
    private double computeAnomalyScore(RawRequestSignals signals) {
        if (isolationForest == null || trainingStructure == null) {
            log.warn("Isolation Forest not initialized");
            return 0.0;
        }

        try {
            // Estrai feature numeriche
            Instance instance = extractFeatures(signals);

            // Predizione (anomaly score)
            double score = isolationForest.classifyInstance(instance);

            // Normalizza score (Weka IF restituisce path length negativo per anomalie)
            return normalizeAnomalyScore(score);

        } catch (Exception e) {
            log.warn("Failed to compute IF score: {}", e.getMessage());
            return 0.0; // Score neutro
        }
    }

    /**
     * Crea la struttura delle feature per Weka
     */
    private Instances createFeatureStructure() {
        ArrayList<Attribute> attributes = new ArrayList<>();

        // Feature numeriche
        attributes.add(new Attribute("uri_length"));
        attributes.add(new Attribute("param_count"));
        attributes.add(new Attribute("header_count"));
        attributes.add(new Attribute("time_since_prev_ms", List.of("normal", "fast", "very_fast")));
        attributes.add(new Attribute("request_rate_per_min"));
        attributes.add(new Attribute("user_agent_entropy"));
        attributes.add(new Attribute("path_depth"));
        attributes.add(new Attribute("query_complexity"));
        attributes.add(new Attribute("canary_trap_score"));
        attributes.add(new Attribute("suspicious_chars_ratio"));
        attributes.add(new Attribute("session_request_count"));
        attributes.add(new Attribute("avg_time_between_requests"));

        // Crea dataset vuoto
        Instances dataset = new Instances("request_features", attributes, 0);
        dataset.setClassIndex(-1); // Unsupervised
        return new Instances(dataset, 0);
    }

    /**
     * Estrae feature numeriche dal RawRequestSignals
     */
    private Instance extractFeatures(RawRequestSignals signals) {
        double[] values = new double[12];

        // 1. Lunghezza URI normalizzata (più lunga = più sospetta in molti attacchi)
        values[0] = Math.log1p(signals.getUri().length()) / 10.0;

        // 2. Numero parametri (attacchi come SQLi spesso hanno molti parametri)
        long paramCount = signals.getUri().chars().filter(c -> c == '&' || c == '=').count();
        if (signals.getQueryParams() != null) {
            paramCount = Math.max(paramCount, signals.getQueryParams().size());
        }
        values[1] = Math.min(paramCount / 10.0, 5.0);

        // 3. Numero headers (bot/scanners spesso hanno meno header dei browser)
        values[2] = signals.getHeaders() != null ? signals.getHeaders().size() / 20.0 : 0;

        // 4. Timing (categorizzato) - cruciale per rilevare automazione
        if (signals.getTimeSincePreviousRequest() != null) {
            if (signals.getTimeSincePreviousRequest() < 100) values[3] = 2; // very_fast
            else if (signals.getTimeSincePreviousRequest() < 1000) values[3] = 1; // fast
            else values[3] = 0; // normal
        } else {
            values[3] = 0; // Primo request della sessione
        }

        // 5. Request rate (usiamo il tempo trascorso come proxy inverso)
        values[4] = (signals.getTimeSincePreviousRequest() != null && signals.getTimeSincePreviousRequest() < 2000) ? 1.0 : 0.0;

        // 6. User agent complexity (proxy per entropy)
        String ua = signals.getUserAgent() != null ? signals.getUserAgent() : "";
        values[5] = ua.length() > 60 ? 1.0 : 0.3;

        // 7. Profondità path (attacchi directory traversal o scansione admin)
        values[6] = (long) signals.getUri().chars().filter(c -> c == '/').count() / 5.0;

        // 8. Complessità query (presenza di caratteri non alfanumerici)
        String query = signals.getUri().contains("?") ? signals.getUri().substring(signals.getUri().indexOf("?")) : "";
        long nonAlpha = query.chars().filter(c -> !Character.isLetterOrDigit(c) && c != '=' && c != '&' && c != '?').count();
        values[7] = Math.min(nonAlpha / 5.0, 2.0);

        // 9. Canary trap (feature "ground truth" per l'anomaly detection)
        values[8] = signals.isCanaryTrapTriggered() ? 1.0 : 0.0;

        // 10. Caratteri sospetti ratio (SQLi, XSS patterns)
        String uriLower = signals.getUri().toLowerCase();
        long suspiciousChars = uriLower.chars()
                .filter(c -> "%<>[]{}|\\^~'\"();".indexOf(c) >= 0).count();
        values[9] = Math.min(suspiciousChars / 5.0, 3.0);

        // 11. Session request count (rilevazione crawler massivi)
        values[10] = signals.getSessionRequestCount() != null ? Math.min(signals.getSessionRequestCount() / 20.0, 5.0) : 0;

        // 12. Average time between requests (rilevazione timing bots precisi)
        values[11] = signals.getAverageTimeBetweenRequests() != null ? Math.min(signals.getAverageTimeBetweenRequests() / 5000.0, 2.0) : 0;

        // Crea instance
        Instance instance = new DenseInstance(1.0, values);
        instance.setDataset(trainingStructure);
        return instance;
    }

    /**
     * Normalizza l'anomaly score di Isolation Forest
     */
    private double normalizeAnomalyScore(double rawScore) {
        // Isolation Forest: più negativo = più anomalo
        // Normalizza in range [-1, 1] dove -1 = molto anomalo
        return (rawScore + 0.5) / 0.5; // Semplice normalizzazione
    }

    /**
     * Main method for this class.
     */
    public static void main(String[] args) {
        // Esempio d'uso standalone rimosso per brevità, 
        // integrato via Spring Component
    }
}