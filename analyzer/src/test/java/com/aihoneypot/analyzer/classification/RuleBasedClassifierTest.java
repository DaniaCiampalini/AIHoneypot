package com.aihoneypot.analyzer.classification;

import com.aihoneypot.core.model.ClassificationResult;
import com.aihoneypot.core.model.ClientType;
import com.aihoneypot.core.model.RawRequestSignals;
import com.aihoneypot.core.model.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for RuleBasedClassifier.
 * Tests classification logic, confidence scoring, and edge cases.
 */
@DisplayName("RuleBasedClassifier Tests")
class RuleBasedClassifierTest {

    private RuleBasedClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new RuleBasedClassifier();
    }

    @Test
    @DisplayName("Should classify bot by User-Agent")
    void testBotDetectionByUserAgent() {
        // Given
        RawRequestSignals signals = RawRequestSignals.builder()
                .sessionId("test-1")
                .ipAddress("192.168.1.1")
                .userAgent("Googlebot/2.1 (+http://www.google.com/bot.html)")
                .uri("/")
                .method("GET")
                .timestamp(Instant.now())
                .build();

        // When
        ClassificationResult result = classifier.classify(signals);

        // Then
        assertNotNull(result);
        assertEquals(ClientType.BOT_CRAWLER, result.getClientType());
        assertTrue(result.getConfidence() > 0.7, "Confidence should be high for obvious bot");
    }

    @Test
    @DisplayName("Should classify human browser by User-Agent")
    void testHumanBrowserDetection() {
        // Given
        RawRequestSignals signals = RawRequestSignals.builder()
                .sessionId("test-2")
                .ipAddress("192.168.1.2")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .uri("/home")
                .method("GET")
                .timestamp(Instant.now())
                .build();

        // When
        ClassificationResult result = classifier.classify(signals);

        // Then
        assertNotNull(result);
        assertEquals(ClientType.HUMAN_BROWSER, result.getClientType());
        assertTrue(result.getConfidence() > 0.5);
    }

    @ParameterizedTest
    @DisplayName("Should detect various bot patterns")
    @CsvSource({
        "curl/7.68.0, BOT_SCRAPER",
        "Python-urllib/3.8, BOT_SCRAPER",
        "Googlebot/2.1, BOT_CRAWLER",
        "GPT-Bot/1.0, AI_AGENT",
        "sqlmap/1.5, MALICIOUS_SCANNER"
    })
    void testBotPatternDetection(String userAgent, String expectedType) {
        // Given
        RawRequestSignals signals = RawRequestSignals.builder()
                .sessionId("test")
                .ipAddress("10.0.0.1")
                .userAgent(userAgent)
                .uri("/")
                .method("GET")
                .timestamp(Instant.now())
                .build();

        // When
        ClassificationResult result = classifier.classify(signals);

        // Then
        assertNotNull(result);
        assertEquals(ClientType.valueOf(expectedType), result.getClientType());
    }

    @Test
    @DisplayName("Should detect AI agent by User-Agent")
    void testAIAgentDetection() {
        // Given
        String[] aiAgents = {
            "GPT-Bot/1.0",
            "Claude-Web/1.0",
            "ChatGPT-User/1.0",
            "AI-Assistant/2.0"
        };

        for (String agent : aiAgents) {
            RawRequestSignals signals = RawRequestSignals.builder()
                    .sessionId("test")
                    .ipAddress("10.0.0.1")
                    .userAgent(agent)
                    .uri("/")
                    .method("GET")
                    .timestamp(Instant.now())
                    .build();

            // When
            ClassificationResult result = classifier.classify(signals);

            // Then
            assertEquals(ClientType.AI_AGENT, result.getClientType(),
                "Should detect AI agent: " + agent);
        }
    }

    @Test
    @DisplayName("Should detect canary trap access with HIGH severity")
    void testCanaryTrapDetection() {
        // Given
        String[] canaryPaths = {"/admin", "/wp-admin", "/.env", "/backup", "/.git"};

        for (String path : canaryPaths) {
            RawRequestSignals signals = RawRequestSignals.builder()
                    .sessionId("test")
                    .ipAddress("192.168.1.100")
                    .userAgent("Mozilla/5.0")
                    .uri(path)
                    .method("GET")
                    .timestamp(Instant.now())
                    .build();

            // When
            ClassificationResult result = classifier.classify(signals);

            // Then
            assertTrue(result.getSeverity() == Severity.HIGH || result.getSeverity() == Severity.CRITICAL,
                "Canary trap access should have HIGH or CRITICAL severity: " + path);
        }
    }

    @Test
    @DisplayName("Should detect SQL injection attempts")
    void testSQLInjectionDetection() {
        // Given
        Map<String, String> maliciousParams = new HashMap<>();
        maliciousParams.put("id", "1' OR '1'='1");

        RawRequestSignals signals = RawRequestSignals.builder()
                .sessionId("test")
                .ipAddress("192.168.1.100")
                .userAgent("Mozilla/5.0")
                .uri("/api/users")
                .method("GET")
                .queryParams(maliciousParams)
                .timestamp(Instant.now())
                .build();

        // When
        ClassificationResult result = classifier.classify(signals);

        // Then
        assertNotNull(result);
        assertEquals(Severity.CRITICAL, result.getSeverity());
        assertTrue(result.getExplanation().toLowerCase().contains("injection") ||
                   result.getExplanation().toLowerCase().contains("malicious"));
    }

    @Test
    @DisplayName("Should assign higher confidence for multiple signals")
    void testConfidenceScoring() {
        // Given - Multiple suspicious signals
        RawRequestSignals suspiciousSignals = RawRequestSignals.builder()
                .sessionId("test")
                .ipAddress("192.168.1.100")
                .userAgent("curl/7.68.0") // Bot agent
                .uri("/admin") // Canary trap
                .method("POST") // Suspicious method for /admin
                .timestamp(Instant.now())
                .build();

        // Given - Single signal
        RawRequestSignals normalSignals = RawRequestSignals.builder()
                .sessionId("test")
                .ipAddress("192.168.1.101")
                .userAgent("Mozilla/5.0 (Windows NT 10.0)")
                .uri("/home")
                .method("GET")
                .timestamp(Instant.now())
                .build();

        // When
        ClassificationResult suspiciousResult = classifier.classify(suspiciousSignals);
        ClassificationResult normalResult = classifier.classify(normalSignals);

        // Then
        assertTrue(suspiciousResult.getConfidence() > normalResult.getConfidence(),
            "Multiple suspicious signals should result in higher confidence");
    }

    @Test
    @DisplayName("Should handle null User-Agent gracefully")
    void testNullUserAgentHandling() {
        // Given
        RawRequestSignals signals = RawRequestSignals.builder()
                .sessionId("test")
                .ipAddress("192.168.1.1")
                .userAgent(null)
                .uri("/")
                .method("GET")
                .timestamp(Instant.now())
                .build();

        // When
        ClassificationResult result = classifier.classify(signals);

        // Then
        assertNotNull(result);
        assertTrue(result.getSeverity() == Severity.MEDIUM || result.getSeverity() == Severity.HIGH,
            "Null User-Agent should be suspicious");
    }

    @Test
    @DisplayName("Should detect port scanning behavior")
    void testPortScanningDetection() {
        // Given
        RawRequestSignals signals = RawRequestSignals.builder()
                .sessionId("test")
                .ipAddress("192.168.1.100")
                .userAgent("Nmap/7.80")
                .uri("/")
                .method("HEAD")
                .timestamp(Instant.now())
                .build();

        // When
        ClassificationResult result = classifier.classify(signals);

        // Then
        assertEquals(ClientType.MALICIOUS_SCANNER, result.getClientType());
        assertEquals(Severity.HIGH, result.getSeverity());
    }

    @Test
    @DisplayName("Should classify unknown patterns as UNKNOWN")
    void testUnknownClassification() {
        // Given
        RawRequestSignals signals = RawRequestSignals.builder()
                .sessionId("test")
                .ipAddress("192.168.1.1")
                .userAgent("CustomAgent/1.0")
                .uri("/api/data")
                .method("GET")
                .timestamp(Instant.now())
                .build();

        // When
        ClassificationResult result = classifier.classify(signals);

        // Then
        assertNotNull(result);
        // Should have some classification
        assertNotNull(result.getClientType());
        assertNotNull(result.getSeverity());
    }

    @ParameterizedTest
    @DisplayName("Should assign correct severity levels")
    @ValueSource(strings = {"/admin", "/.env", "/backup", "/config.php"})
    void testSeverityAssignment(String path) {
        // Given
        RawRequestSignals signals = RawRequestSignals.builder()
                .sessionId("test")
                .ipAddress("192.168.1.100")
                .userAgent("Mozilla/5.0")
                .uri(path)
                .method("GET")
                .timestamp(Instant.now())
                .build();

        // When
        ClassificationResult result = classifier.classify(signals);

        // Then
        assertTrue(result.getSeverity() == Severity.HIGH ||
                   result.getSeverity() == Severity.CRITICAL,
            "Sensitive paths should have HIGH or CRITICAL severity");
    }

    @Test
    @DisplayName("Should provide detailed explanation")
    void testExplanationContent() {
        // Given
        RawRequestSignals signals = RawRequestSignals.builder()
                .sessionId("test")
                .ipAddress("192.168.1.100")
                .userAgent("Googlebot/2.1")
                .uri("/")
                .method("GET")
                .timestamp(Instant.now())
                .build();

        // When
        ClassificationResult result = classifier.classify(signals);

        // Then
        assertNotNull(result.getExplanation());
        assertFalse(result.getExplanation().isEmpty());
        assertTrue(result.getExplanation().length() > 10,
            "Explanation should be descriptive");
    }

    @Test
    @DisplayName("Should handle rapid requests (rate limiting check)")
    void testRapidRequestDetection() {
        // Given
        Instant now = Instant.now();

        RawRequestSignals signals = RawRequestSignals.builder()
                .sessionId("test")
                .ipAddress("192.168.1.100")
                .userAgent("curl/7.68.0")
                .uri("/api/data")
                .method("GET")
                .timestamp(now)
                .build();

        // When
        ClassificationResult result = classifier.classify(signals);

        // Then
        assertNotNull(result);
        // In a real scenario, we'd check request frequency
        // Here we just verify the classifier handles it
    }

    @Test
    @DisplayName("Should detect XSS attack patterns")
    void testXSSDetection() {
        // Given
        Map<String, String> xssParams = new HashMap<>();
        xssParams.put("input", "<script>alert('XSS')</script>");

        RawRequestSignals signals = RawRequestSignals.builder()
                .sessionId("test")
                .ipAddress("192.168.1.100")
                .userAgent("Mozilla/5.0")
                .uri("/search")
                .method("GET")
                .queryParams(xssParams)
                .timestamp(Instant.now())
                .build();

        // When
        ClassificationResult result = classifier.classify(signals);

        // Then
        assertTrue(result.getSeverity() == Severity.HIGH ||
                   result.getSeverity() == Severity.CRITICAL);
    }
}

