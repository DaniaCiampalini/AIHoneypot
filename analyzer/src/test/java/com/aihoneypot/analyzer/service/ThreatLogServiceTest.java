package com.aihoneypot.analyzer.service;

import com.aihoneypot.analyzer.entity.ThreatSession;
import com.aihoneypot.analyzer.repository.ThreatSessionRepository;
import com.aihoneypot.core.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ThreatLogService.
 *
 * The repository is mocked with Mockito — no database or Spring context needed.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ThreatLogService")
class ThreatLogServiceTest {

    @Mock
    private ThreatSessionRepository repository;

    @InjectMocks
    private ThreatLogService service;

    // ── Test data builders ──────────────────────────────────────────────────────

    private ClassificationResult threat(String sessionId) {
        return ClassificationResult.builder()
                .sessionId(sessionId)
                .timestamp(Instant.now())
                .clientType(ClientType.BOT_SCRAPER)
                .confidence(0.85)
                .severity(Severity.HIGH)
                .isThreat(true)
                .anomalyScore(0.85)
                .explanation("Bot-like User-Agent.")
                .classifierName("RuleBased")
                .build();
    }

    private ClassificationResult nonThreat(String sessionId) {
        return ClassificationResult.builder()
                .sessionId(sessionId)
                .timestamp(Instant.now())
                .clientType(ClientType.HUMAN_BROWSER)
                .confidence(0.10)
                .severity(Severity.LOW)
                .isThreat(false)
                .anomalyScore(0.10)
                .explanation("Normal behavior detected.")
                .classifierName("RuleBased")
                .build();
    }

    private RawRequestSignals signals(String sessionId) {
        return RawRequestSignals.builder()
                .sessionId(sessionId)
                .ipAddress("1.2.3.4")
                .method("GET")
                .uri("/admin")
                .userAgent("Googlebot/2.1")
                .timestamp(Instant.now())
                .canaryTrapTriggered(true)
                .build();
    }

    @BeforeEach
    void setUp() {
        // Default: session not already logged
        when(repository.findBySessionId(any())).thenReturn(Optional.empty());
    }

    // ── logThreat: persistence ──────────────────────────────────────────────────

    @Nested
    @DisplayName("logThreat — persistence")
    class LogThreatPersistence {

        @Test
        @DisplayName("Threat with isThreat=true is saved to repository")
        void threatIsSaved() {
            service.logThreat(threat("s1"), signals("s1"));
            verify(repository, times(1)).save(any(ThreatSession.class));
        }

        @Test
        @DisplayName("Non-threat with low confidence (< 0.5) is NOT saved")
        void nonThreatLowConfidenceNotSaved() {
            service.logThreat(nonThreat("s1"), signals("s1"));
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Non-threat with high confidence (>= 0.5) IS saved")
        void nonThreatHighConfidenceSaved() {
            ClassificationResult highConfidenceNonThreat = ClassificationResult.builder()
                    .sessionId("s1")
                    .timestamp(Instant.now())
                    .clientType(ClientType.UNKNOWN)
                    .confidence(0.60) // >= 0.5 but isThreat=false
                    .severity(Severity.MEDIUM)
                    .isThreat(false)
                    .anomalyScore(0.60)
                    .explanation("Suspicious.")
                    .classifierName("RuleBased")
                    .build();
            service.logThreat(highConfidenceNonThreat, signals("s1"));
            verify(repository, times(1)).save(any(ThreatSession.class));
        }

        @Test
        @DisplayName("Already-logged session (findBySessionId returns value) is NOT saved again")
        void duplicateSessionNotSaved() {
            ThreatSession existing = ThreatSession.builder()
                    .sessionId("s1").ipAddress("1.2.3.4")
                    .timestamp(Instant.now()).clientType(ClientType.BOT_SCRAPER)
                    .confidence(0.8).severity(Severity.HIGH).isThreat(true)
                    .build();
            when(repository.findBySessionId("s1")).thenReturn(Optional.of(existing));

            service.logThreat(threat("s1"), signals("s1"));
            verify(repository, never()).save(any());
        }
    }

    // ── logThreat: entity field mapping ────────────────────────────────────────

    @Nested
    @DisplayName("logThreat — entity field mapping")
    class LogThreatFieldMapping {

        private ThreatSession capturesSaved() {
            ArgumentCaptor<ThreatSession> captor = ArgumentCaptor.forClass(ThreatSession.class);
            verify(repository).save(captor.capture());
            return captor.getValue();
        }

        @Test
        @DisplayName("sessionId is copied from result")
        void sessionIdMapped() {
            service.logThreat(threat("my-session"), signals("my-session"));
            assertEquals("my-session", capturesSaved().getSessionId());
        }

        @Test
        @DisplayName("ipAddress is taken from signals (not result)")
        void ipAddressTakenFromSignals() {
            RawRequestSignals sig = signals("s1");
            service.logThreat(threat("s1"), sig);
            assertEquals("1.2.3.4", capturesSaved().getIpAddress());
        }

        @Test
        @DisplayName("clientType is copied from result")
        void clientTypeMapped() {
            service.logThreat(threat("s1"), signals("s1"));
            assertEquals(ClientType.BOT_SCRAPER, capturesSaved().getClientType());
        }

        @Test
        @DisplayName("confidence is copied from result")
        void confidenceMapped() {
            service.logThreat(threat("s1"), signals("s1"));
            assertEquals(0.85, capturesSaved().getConfidence(), 0.001);
        }

        @Test
        @DisplayName("severity is copied from result")
        void severityMapped() {
            service.logThreat(threat("s1"), signals("s1"));
            assertEquals(Severity.HIGH, capturesSaved().getSeverity());
        }

        @Test
        @DisplayName("isThreat is copied from result")
        void isThreatMapped() {
            service.logThreat(threat("s1"), signals("s1"));
            assertTrue(capturesSaved().getIsThreat());
        }

        @Test
        @DisplayName("userAgent is taken from signals")
        void userAgentFromSignals() {
            service.logThreat(threat("s1"), signals("s1"));
            assertEquals("Googlebot/2.1", capturesSaved().getUserAgent());
        }

        @Test
        @DisplayName("firstUri is taken from signals")
        void firstUriFromSignals() {
            service.logThreat(threat("s1"), signals("s1"));
            assertEquals("/admin", capturesSaved().getFirstUri());
        }

        @Test
        @DisplayName("canaryTrapTriggered is taken from signals")
        void canaryTrapFromSignals() {
            service.logThreat(threat("s1"), signals("s1"));
            assertTrue(capturesSaved().getCanaryTrapTriggered());
        }

        @Test
        @DisplayName("explanation is copied from result")
        void explanationMapped() {
            service.logThreat(threat("s1"), signals("s1"));
            assertEquals("Bot-like User-Agent.", capturesSaved().getExplanation());
        }

        @Test
        @DisplayName("classifierName is copied from result")
        void classifierNameMapped() {
            service.logThreat(threat("s1"), signals("s1"));
            assertEquals("RuleBased", capturesSaved().getClassifierName());
        }

        @Test
        @DisplayName("requestCount is initialized to 1")
        void requestCountInitializedToOne() {
            service.logThreat(threat("s1"), signals("s1"));
            assertEquals(1, capturesSaved().getRequestCount());
        }
    }

    // ── updateRequestCount ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateRequestCount")
    class UpdateRequestCount {

        @Test
        @DisplayName("Existing session gets its requestCount updated and re-saved")
        void existingSessionUpdated() {
            ThreatSession existing = ThreatSession.builder()
                    .sessionId("s1").ipAddress("1.2.3.4").timestamp(Instant.now())
                    .clientType(ClientType.BOT_SCRAPER).confidence(0.8)
                    .severity(Severity.HIGH).isThreat(true).requestCount(1)
                    .build();
            when(repository.findBySessionId("s1")).thenReturn(Optional.of(existing));

            service.updateRequestCount("s1", 5);

            ArgumentCaptor<ThreatSession> captor = ArgumentCaptor.forClass(ThreatSession.class);
            verify(repository).save(captor.capture());
            assertEquals(5, captor.getValue().getRequestCount());
        }

        @Test
        @DisplayName("Non-existent session → no save is called")
        void nonExistentSessionNoSave() {
            when(repository.findBySessionId("s999")).thenReturn(Optional.empty());
            service.updateRequestCount("s999", 10);
            verify(repository, never()).save(any());
        }
    }

    // ── isSessionLogged ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("isSessionLogged")
    class IsSessionLogged {

        @Test
        @DisplayName("Returns true when session exists in repository")
        void returnsTrueWhenPresent() {
            ThreatSession existing = ThreatSession.builder()
                    .sessionId("s1").ipAddress("1.2.3.4").timestamp(Instant.now())
                    .clientType(ClientType.BOT_SCRAPER).confidence(0.8)
                    .severity(Severity.HIGH).isThreat(true).build();
            when(repository.findBySessionId("s1")).thenReturn(Optional.of(existing));
            assertTrue(service.isSessionLogged("s1"));
        }

        @Test
        @DisplayName("Returns false when session does not exist in repository")
        void returnsFalseWhenAbsent() {
            when(repository.findBySessionId("missing")).thenReturn(Optional.empty());
            assertFalse(service.isSessionLogged("missing"));
        }

        @Test
        @DisplayName("Delegates to repository.findBySessionId")
        void delegatesToRepository() {
            when(repository.findBySessionId("s1")).thenReturn(Optional.empty());
            service.isSessionLogged("s1");
            verify(repository, times(1)).findBySessionId("s1");
        }
    }
}