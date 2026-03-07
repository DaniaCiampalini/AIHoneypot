package com.aihoneypot.collector.service;

import com.aihoneypot.core.model.RawRequestSignals;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store for session tracking and request history.
 * Tracks multiple requests per session to enable behavioral analysis.
 */
@Slf4j
@Service
public class SessionStore {

    private final Map<String, SessionData> sessions = new ConcurrentHashMap<>();

    private static final int MAX_REQUESTS_PER_SESSION = 100;
    private static final long SESSION_TIMEOUT_MS = 30 * 60 * 1000; // 30 minutes

    /**
     * Add a request to a session.
     *
     * @param sessionId The session identifier
     * @param signals The raw request signals
     */
    public void addRequest(String sessionId, RawRequestSignals signals) {
        sessions.compute(sessionId, (id, data) -> {
            if (data == null) {
                data = new SessionData(sessionId);
                log.info("New session created: {}", sessionId);
            }
            data.addRequest(signals);
            return data;
        });

        // Clean up old sessions periodically
        if (sessions.size() % 100 == 0) {
            cleanupExpiredSessions();
        }
    }

    /**
     * Get all requests for a session.
     *
     * @param sessionId The session identifier
     * @return List of raw request signals, or empty list if session not found
     */
    public List<RawRequestSignals> getSessionRequests(String sessionId) {
        SessionData data = sessions.get(sessionId);
        return data != null ? new ArrayList<>(data.requests) : Collections.emptyList();
    }

    /**
     * Get the previous request for a session.
     *
     * @param sessionId The session identifier
     * @return The previous request, or null if this is the first request
     */
    public RawRequestSignals getPreviousRequest(String sessionId) {
        SessionData data = sessions.get(sessionId);
        if (data != null && data.requests.size() > 1) {
            return data.requests.get(data.requests.size() - 2);
        }
        return null;
    }

    /**
     * Get request count for a session.
     *
     * @param sessionId The session identifier
     * @return Number of requests in this session
     */
    public int getRequestCount(String sessionId) {
        SessionData data = sessions.get(sessionId);
        return data != null ? data.requests.size() : 0;
    }

    /**
     * Remove a session from the store.
     *
     * @param sessionId The session identifier
     */
    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
        log.debug("Session removed: {}", sessionId);
    }

    /**
     * Clean up expired sessions.
     */
    private void cleanupExpiredSessions() {
        Instant now = Instant.now();
        sessions.entrySet().removeIf(entry -> {
            long age = now.toEpochMilli() - entry.getValue().lastAccessTime.toEpochMilli();
            return age > SESSION_TIMEOUT_MS;
        });
    }

    /**
     * Get total number of active sessions.
     *
     * @return Session count
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }

    /**
     * Internal class to track session data.
     */
    private static class SessionData {
        private final String sessionId;
        private final List<RawRequestSignals> requests;
        private Instant lastAccessTime;

        SessionData(String sessionId) {
            this.sessionId = sessionId;
            this.requests = new ArrayList<>();
            this.lastAccessTime = Instant.now();
        }

        void addRequest(RawRequestSignals signals) {
            if (requests.size() >= MAX_REQUESTS_PER_SESSION) {
                requests.remove(0); // Remove oldest
            }
            requests.add(signals);
            lastAccessTime = Instant.now();
        }
    }
}

