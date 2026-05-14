package com.aihoneypot.analyzer.service;

import com.aihoneypot.analyzer.entity.ThreatSession;
import com.aihoneypot.analyzer.repository.ThreatSessionRepository;
import com.aihoneypot.core.model.ClassificationResult;
import com.aihoneypot.core.model.ClientType;
import com.aihoneypot.core.model.RawRequestSignals;
import com.aihoneypot.core.model.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ThreatLogService Tests")
class ThreatLogServiceTest {

    @Mock
    private ThreatSessionRepository repository;

    @InjectMocks
    private ThreatLogService service;

    private ClassificationResult classificationResult;
    private RawRequestSignals rawSignals;

    @BeforeEach
    void setUp() {
        Instant now = Instant.now();

        classificationResult = ClassificationResult.builder()
                .sessionId("session-test-001")
                .timestamp(now)
                .clientType(ClientType.BOT_SCRAPER)
                .confidence(0.95)
                .severity(Severity.CRITICAL)
                .isThreat(true)
                .anomalyScore(0.88)
                .explanation("Bot detected scanning admin panel")
                .classifierName("BotClassifier")
                .build();

        rawSignals = RawRequestSignals.builder()
                .ipAddress("192.168.1.100")
                .userAgent("Mozilla/5.0 Bot")
                .uri("/admin")
                .canaryTrapTriggered(true)
                .build();
    }

    @Test
    @DisplayName("Should log a valid threat to database")
    void testLogThreatSuccess() {
        when(repository.findBySessionId(anyString())).thenReturn(Optional.empty());

        service.logThreat(classificationResult, rawSignals);

        ArgumentCaptor<ThreatSession> captor = ArgumentCaptor.forClass(ThreatSession.class);
        verify(repository, times(1)).save(captor.capture());

        ThreatSession saved = captor.getValue();
        assertThat(saved.getSessionId()).isEqualTo("session-test-001");
        assertThat(saved.getIpAddress()).isEqualTo("192.168.1.100");
        assertThat(saved.getSeverity()).isEqualTo(Severity.CRITICAL);
        assertThat(saved.getCanaryTrapTriggered()).isEqualTo(true);
    }

    @Test
    @DisplayName("Should skip non-threat with low confidence")
    void testSkipNonThreatLowConfidence() {
        ClassificationResult lowConfidenceResult = ClassificationResult.builder()
                .sessionId("session-low-conf")
                .timestamp(Instant.now())
                .clientType(ClientType.HUMAN_BROWSER)
                .confidence(0.30)
                .severity(Severity.LOW)
                .isThreat(false)
                .anomalyScore(0.20)
                .explanation("Low confidence")
                .classifierName("TestClassifier")
                .build();

        service.logThreat(lowConfidenceResult, rawSignals);

        verify(repository, never()).save(any(ThreatSession.class));
    }

    @Test
    @DisplayName("Should log non-threat with high confidence")
    void testLogNonThreatHighConfidence() {
        ClassificationResult highConfidenceNonThreat = ClassificationResult.builder()
                .sessionId("session-safe-high-conf")
                .timestamp(Instant.now())
                .clientType(ClientType.HUMAN_BROWSER)
                .confidence(0.92)
                .severity(Severity.LOW)
                .isThreat(false)
                .anomalyScore(0.05)
                .explanation("Legitimate user")
                .classifierName("LegitimacyClassifier")
                .build();

        when(repository.findBySessionId(anyString())).thenReturn(Optional.empty());

        service.logThreat(highConfidenceNonThreat, rawSignals);

        verify(repository, times(1)).save(any(ThreatSession.class));
    }

    @Test
    @DisplayName("Should skip already logged session")
    void testSkipAlreadyLoggedSession() {
        ThreatSession existingSession = ThreatSession.builder()
                .id(1L)
                .sessionId("session-test-001")
                .ipAddress("192.168.1.100")
                .build();

        when(repository.findBySessionId("session-test-001"))
                .thenReturn(Optional.of(existingSession));

        service.logThreat(classificationResult, rawSignals);

        verify(repository, never()).save(any(ThreatSession.class));
    }

    @Test
    @DisplayName("Should update request count for existing session")
    void testUpdateRequestCount() {
        ThreatSession existingSession = ThreatSession.builder()
                .id(1L)
                .sessionId("session-test-001")
                .ipAddress("192.168.1.100")
                .requestCount(1)
                .build();

        when(repository.findBySessionId("session-test-001"))
                .thenReturn(Optional.of(existingSession));

        service.updateRequestCount("session-test-001", 5);

        ArgumentCaptor<ThreatSession> captor = ArgumentCaptor.forClass(ThreatSession.class);
        verify(repository, times(1)).save(captor.capture());

        assertThat(captor.getValue().getRequestCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should not update request count for non-existent session")
    void testUpdateRequestCountNonExistent() {
        when(repository.findBySessionId(anyString())).thenReturn(Optional.empty());

        service.updateRequestCount("non-existent", 5);

        verify(repository, never()).save(any(ThreatSession.class));
    }

    @Test
    @DisplayName("Should return true when session is logged")
    void testIsSessionLoggedTrue() {
        ThreatSession existingSession = ThreatSession.builder()
                .id(1L)
                .sessionId("session-test-001")
                .build();

        when(repository.findBySessionId("session-test-001"))
                .thenReturn(Optional.of(existingSession));

        boolean isLogged = service.isSessionLogged("session-test-001");

        assertThat(isLogged).isTrue();
    }

    @Test
    @DisplayName("Should return false when session is not logged")
    void testIsSessionLoggedFalse() {
        when(repository.findBySessionId(anyString())).thenReturn(Optional.empty());

        boolean isLogged = service.isSessionLogged("non-existent");

        assertThat(isLogged).isFalse();
    }

    @Test
    @DisplayName("Should preserve all threat data when logging")
    void testLogThreatPreservesAllData() {
        when(repository.findBySessionId(anyString())).thenReturn(Optional.empty());

        service.logThreat(classificationResult, rawSignals);

        ArgumentCaptor<ThreatSession> captor = ArgumentCaptor.forClass(ThreatSession.class);
        verify(repository, times(1)).save(captor.capture());

        ThreatSession saved = captor.getValue();
        assertThat(saved.getSessionId()).isEqualTo(classificationResult.getSessionId());
        assertThat(saved.getConfidence()).isEqualTo(classificationResult.getConfidence());
        assertThat(saved.getAnomalyScore()).isEqualTo(classificationResult.getAnomalyScore());
        assertThat(saved.getExplanation()).isEqualTo(classificationResult.getExplanation());
        assertThat(saved.getClassifierName()).isEqualTo(classificationResult.getClassifierName());
        assertThat(saved.getUserAgent()).isEqualTo(rawSignals.getUserAgent());
        assertThat(saved.getFirstUri()).isEqualTo(rawSignals.getUri());
    }
}
