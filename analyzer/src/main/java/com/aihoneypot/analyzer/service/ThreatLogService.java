package com.aihoneypot.analyzer.service;

import com.aihoneypot.analyzer.entity.ThreatSession;
import com.aihoneypot.analyzer.repository.ThreatSessionRepository;
import com.aihoneypot.core.model.ClassificationResult;
import com.aihoneypot.core.model.RawRequestSignals;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for logging and persisting threat detections.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThreatLogService {

    private final ThreatSessionRepository repository;

    /**
     * Log a threat detection to the database.
     *
     * @param result The classification result
     * @param signals The raw signals that triggered the classification
     */
    @Transactional
    public void logThreat(ClassificationResult result, RawRequestSignals signals) {
        // Only persist if it's actually a threat or high confidence
        if (!result.isThreat() && result.getConfidence() < 0.5) {
            log.debug("Skipping non-threat with low confidence: {}", result.getSessionId());
            return;
        }

        // Check if we already have this session
        if (repository.findBySessionId(result.getSessionId()).isPresent()) {
            log.debug("Threat session already logged: {}", result.getSessionId());
            return;
        }

        ThreatSession entity = new ThreatSession(
            null,
            result.getSessionId(),
            signals.getIpAddress(),
            result.getTimestamp(),
            result.getClientType(),
            result.getConfidence(),
            result.getSeverity(),
            result.isThreat(),
            result.getAnomalyScore(),
            signals.getUserAgent(),
            signals.getUri(),
            1,
            result.getExplanation(),
            result.getClassifierName(),
            signals.isCanaryTrapTriggered()
        );

        repository.save(entity);

        log.info("Threat logged - Session: {}, Type: {}, Severity: {}, IP: {}",
            result.getSessionId(),
            result.getClientType(),
            result.getSeverity(),
            signals.getIpAddress()
        );
    }

    /**
     * Update request count for an existing threat session.
     *
     * @param sessionId The session identifier
     */
    @Transactional
    public void updateRequestCount(String sessionId, int count) {
        repository.findBySessionId(sessionId).ifPresent(session -> {
            session.setRequestCount(count);
            repository.save(session);
        });
    }

    /**
     * Check if a session is already logged as a threat.
     *
     * @param sessionId The session identifier
     * @return true if the session is in the database
     */
    public boolean isSessionLogged(String sessionId) {
        return repository.findBySessionId(sessionId).isPresent();
    }
}

