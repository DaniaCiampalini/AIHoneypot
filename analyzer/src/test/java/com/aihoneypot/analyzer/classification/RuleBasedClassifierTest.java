package com.aihoneypot.analyzer.classification;

import com.aihoneypot.core.model.ClassificationResult;
import com.aihoneypot.core.model.ClientType;
import com.aihoneypot.core.model.RawRequestSignals;
import com.aihoneypot.core.model.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RuleBasedClassifier.
 * Each test targets a specific rule in the classifier and verifies
 * the exact scoring, client type, severity, and explanation output.
 * No Spring context is loaded — pure unit test.
 */
@DisplayName("RuleBasedClassifier")
class RuleBasedClassifierTest {

    private RuleBasedClassifier classifier;

    // ── Helpers ────────────────────────────────────────────────────────────────

    /**
     * Minimal "clean" signal that scores 0 threat points on its own.
     * Tests add suspicious fields on top of this.
     */
    private RawRequestSignals.RawRequestSignalsBuilder cleanSignals() {
        return RawRequestSignals.builder()
                .sessionId("session-test")
                .ipAddress("1.2.3.4")
                .method("GET")
                .uri("/")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .acceptLanguage("en-US,en;q=0.9")
                .timestamp(Instant.now());
    }

    @BeforeEach
    void setUp() {
        classifier = new RuleBasedClassifier();
    }

    // ── Metadata ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Classifier metadata")
    class Metadata {

        @Test
        @DisplayName("getName() returns 'RuleBased'")
        void getName() {
            assertEquals("RuleBased", classifier.getName());
        }

        @Test
        @DisplayName("isReady() returns true (no model loading required)")
        void isReady() {
            assertTrue(classifier.isReady());
        }
    }

    // ── Result structure ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Result structure")
    class ResultStructure {

        @Test
        @DisplayName("Result is never null")
        void resultNotNull() {
            assertNotNull(classifier.classify(cleanSignals().build()));
        }

        @Test
        @DisplayName("Result preserves session ID from signals")
        void sessionIdPreserved() {
            RawRequestSignals signals = cleanSignals().sessionId("my-session-42").build();
            ClassificationResult result = classifier.classify(signals);
            assertEquals("my-session-42", result.getSessionId());
        }

        @Test
        @DisplayName("Result has a non-null timestamp")
        void timestampSet() {
            assertNotNull(classifier.classify(cleanSignals().build()).getTimestamp());
        }

        @Test
        @DisplayName("Classifier name is embedded in result")
        void classifierNameInResult() {
            assertEquals("RuleBased", classifier.classify(cleanSignals().build()).getClassifierName());
        }

        @Test
        @DisplayName("Explanation is never null or blank")
        void explanationPresent() {
            ClassificationResult result = classifier.classify(cleanSignals().build());
            assertNotNull(result.getExplanation());
            assertFalse(result.getExplanation().isBlank());
        }

        @Test
        @DisplayName("triggeredFeatures map is present")
        void triggeredFeaturesPresent() {
            assertNotNull(classifier.classify(cleanSignals().build()).getTriggeredFeatures());
        }

        @Test
        @DisplayName("Confidence is between 0.0 and 1.0")
        void confidenceRange() {
            ClassificationResult result = classifier.classify(cleanSignals().build());
            assertTrue(result.getConfidence() >= 0.0 && result.getConfidence() <= 1.0);
        }
    }

    // ── Clean human browser ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("Clean human browser")
    class HumanBrowser {

        @Test
        @DisplayName("Full headers, normal UA → HUMAN_BROWSER")
        void classifiedAsHuman() {
            ClassificationResult result = classifier.classify(cleanSignals().build());
            assertEquals(ClientType.HUMAN_BROWSER, result.getClientType());
        }

        @Test
        @DisplayName("Clean request is not a threat")
        void notAThreat() {
            assertFalse(classifier.classify(cleanSignals().build()).isThreat());
        }

        @Test
        @DisplayName("Clean request severity is LOW")
        void severityLow() {
            assertEquals(Severity.LOW, classifier.classify(cleanSignals().build()).getSeverity());
        }

        @Test
        @DisplayName("Clean request confidence is below 0.30 (threat threshold)")
        void confidenceLow() {
            assertTrue(classifier.classify(cleanSignals().build()).getConfidence() < 0.30);
        }
    }

    // ── Canary trap (Rule 1: +50 points) ───────────────────────────────────────

    @Nested
    @DisplayName("Rule 1 — Canary trap")
    class CanaryTrap {

        @Test
        @DisplayName("canaryTrapTriggered=true adds 'canary_trap' to triggeredFeatures")
        void canaryFeaturePresent() {
            RawRequestSignals signals = cleanSignals().canaryTrapTriggered(true).build();
            assertTrue(classifier.classify(signals).getTriggeredFeatures().containsKey("canary_trap"));
        }

        @Test
        @DisplayName("Canary trap alone → isThreat=true (50 pts >= 30 threshold)")
        void canaryAloneIsThreat() {
            RawRequestSignals signals = cleanSignals().canaryTrapTriggered(true).build();
            assertTrue(classifier.classify(signals).isThreat());
        }

        @Test
        @DisplayName("Canary trap alone → HIGH severity (50 pts)")
        void canaryAloneHighSeverity() {
            RawRequestSignals signals = cleanSignals().canaryTrapTriggered(true).build();
            assertEquals(Severity.HIGH, classifier.classify(signals).getSeverity());
        }

        @Test
        @DisplayName("Canary trap explanation mentions canary")
        void canaryExplanation() {
            RawRequestSignals signals = cleanSignals().canaryTrapTriggered(true).build();
            String explanation = classifier.classify(signals).getExplanation().toLowerCase();
            assertTrue(explanation.contains("canary"), "Expected 'canary' in: " + explanation);
        }

        @Test
        @DisplayName("Canary + missing UA → CRITICAL severity (50+25 = 75 pts)")
        void canaryPlusMissingUaCritical() {
            RawRequestSignals signals = cleanSignals()
                    .canaryTrapTriggered(true)
                    .userAgent(null)
                    .build();
            assertEquals(Severity.CRITICAL, classifier.classify(signals).getSeverity());
        }
    }

    // ── Missing Accept header (Rule 2: +15 points) ─────────────────────────────

    @Nested
    @DisplayName("Rule 2 — Missing Accept header")
    class MissingAcceptHeader {

        @Test
        @DisplayName("Null Accept header → 'missing_accept_header' feature")
        void nullAcceptAddsFeature() {
            RawRequestSignals signals = cleanSignals().acceptHeader(null).build();
            assertTrue(classifier.classify(signals).getTriggeredFeatures()
                    .containsKey("missing_accept_header"));
        }

        @Test
        @DisplayName("Empty Accept header → 'missing_accept_header' feature")
        void emptyAcceptAddsFeature() {
            RawRequestSignals signals = cleanSignals().acceptHeader("").build();
            assertTrue(classifier.classify(signals).getTriggeredFeatures()
                    .containsKey("missing_accept_header"));
        }

        @Test
        @DisplayName("Present Accept header does NOT add feature")
        void presentAcceptNoFeature() {
            RawRequestSignals signals = cleanSignals()
                    .acceptHeader("text/html,application/xhtml+xml").build();
            assertFalse(classifier.classify(signals).getTriggeredFeatures()
                    .containsKey("missing_accept_header"));
        }
    }

    // ── Missing Accept-Language (Rule 3: +10 points) ───────────────────────────

    @Nested
    @DisplayName("Rule 3 — Missing Accept-Language header")
    class MissingAcceptLanguage {

        @Test
        @DisplayName("Null Accept-Language → 'missing_accept_language' feature")
        void nullAcceptLanguageAddsFeature() {
            RawRequestSignals signals = cleanSignals().acceptLanguage(null).build();
            assertTrue(classifier.classify(signals).getTriggeredFeatures()
                    .containsKey("missing_accept_language"));
        }

        @Test
        @DisplayName("Empty Accept-Language → 'missing_accept_language' feature")
        void emptyAcceptLanguageAddsFeature() {
            RawRequestSignals signals = cleanSignals().acceptLanguage("").build();
            assertTrue(classifier.classify(signals).getTriggeredFeatures()
                    .containsKey("missing_accept_language"));
        }
    }

    // ── User-Agent analysis (Rule 4) ────────────────────────────────────────────

    @Nested
    @DisplayName("Rule 4 — User-Agent analysis")
    class UserAgentAnalysis {

        @Test
        @DisplayName("Null UA → 'missing_user_agent' feature (+25 pts)")
        void nullUaAddsFeature() {
            RawRequestSignals signals = cleanSignals().userAgent(null).build();
            assertTrue(classifier.classify(signals).getTriggeredFeatures()
                    .containsKey("missing_user_agent"));
        }

        @ParameterizedTest
        @DisplayName("Bot keyword in UA → BOT_SCRAPER + 'bot_user_agent' feature")
        @ValueSource(strings = {"Googlebot/2.1", "MySpider/1.0", "WebCrawler", "AutoScraper"})
        void botKeywordDetected(String ua) {
            RawRequestSignals signals = cleanSignals().userAgent(ua).build();
            ClassificationResult result = classifier.classify(signals);
            assertEquals(ClientType.BOT_SCRAPER, result.getClientType());
            assertTrue(result.getTriggeredFeatures().containsKey("bot_user_agent"));
        }

        @ParameterizedTest
        @DisplayName("AI agent keyword in UA → AI_AGENT + 'ai_agent_user_agent' feature")
        @ValueSource(strings = {
                "GPT-Bot/1.0",
                "openai-link-uploader/1.0",
                "Claude-Web/1.0",
                "anthropic-ai/1.0",
                "LangChainAgent/0.1",
                "MyLLMBot/2.0"
        })
        void aiAgentKeywordDetected(String ua) {
            RawRequestSignals signals = cleanSignals().userAgent(ua).build();
            ClassificationResult result = classifier.classify(signals);
            assertEquals(ClientType.AI_AGENT, result.getClientType());
            assertTrue(result.getTriggeredFeatures().containsKey("ai_agent_user_agent"));
        }

        @ParameterizedTest
        @DisplayName("Security scanner keyword in UA → SECURITY_SCANNER + 'security_scanner' feature")
        @ValueSource(strings = {"Nikto/2.1.6", "nmap-smb/7.80", "masscan/1.0", "BurpSuite/2023"})
        void securityScannerDetected(String ua) {
            RawRequestSignals signals = cleanSignals().userAgent(ua).build();
            ClassificationResult result = classifier.classify(signals);
            assertEquals(ClientType.SECURITY_SCANNER, result.getClientType());
            assertTrue(result.getTriggeredFeatures().containsKey("security_scanner"));
        }

        @Test
        @DisplayName("AI agent UA is a threat with confidence >= 0.30")
        void aiAgentIsThreat() {
            RawRequestSignals signals = cleanSignals().userAgent("GPT-Bot/1.0").build();
            ClassificationResult result = classifier.classify(signals);
            assertTrue(result.isThreat());
            assertTrue(result.getConfidence() >= 0.30);
        }

        @Test
        @DisplayName("Security scanner UA has MEDIUM severity (40pts)")
        void securityScannerSeverity() {
            RawRequestSignals signals = cleanSignals().userAgent("Nikto/2.1.6").build();
            ClassificationResult result = classifier.classify(signals);
            assertEquals(Severity.MEDIUM, result.getSeverity());
        }

        @Test
        @DisplayName("AI agent UA explanation mentions 'AI agent'")
        void aiAgentExplanation() {
            RawRequestSignals signals = cleanSignals().userAgent("claude/1.0").build();
            String explanation = classifier.classify(signals).getExplanation().toLowerCase();
            assertTrue(explanation.contains("ai agent"), "Expected 'AI agent' in: " + explanation);
        }

        @ParameterizedTest
        @DisplayName("Normal browser UA is not flagged as bot/AI/scanner")
        @ValueSource(strings = {
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15",
                "Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/115.0"
        })
        void normalBrowserNotFlagged(String ua) {
            RawRequestSignals signals = cleanSignals().userAgent(ua).build();
            ClassificationResult result = classifier.classify(signals);
            assertFalse(result.getTriggeredFeatures().containsKey("bot_user_agent"));
            assertFalse(result.getTriggeredFeatures().containsKey("ai_agent_user_agent"));
            assertFalse(result.getTriggeredFeatures().containsKey("security_scanner"));
            assertFalse(result.getTriggeredFeatures().containsKey("missing_user_agent"));
        }
    }

    // ── Fast requests (Rule 6: +20 points) ─────────────────────────────────────

    @Nested
    @DisplayName("Rule 6 — Fast requests")
    class FastRequests {

        @Test
        @DisplayName("timeSincePreviousRequest < 100ms → 'fast_requests' feature")
        void fastRequestAddsFeature() {
            RawRequestSignals signals = cleanSignals()
                    .timeSincePreviousRequest(50L)
                    .build();
            assertTrue(classifier.classify(signals).getTriggeredFeatures()
                    .containsKey("fast_requests"));
        }

        @Test
        @DisplayName("fast_requests feature value equals the actual timing value")
        void fastRequestFeatureValue() {
            RawRequestSignals signals = cleanSignals()
                    .timeSincePreviousRequest(42L)
                    .build();
            Object value = classifier.classify(signals).getTriggeredFeatures().get("fast_requests");
            assertEquals(42L, value);
        }

        @Test
        @DisplayName("timeSincePreviousRequest == 100ms is NOT flagged (boundary)")
        void exactBoundaryNotFlagged() {
            RawRequestSignals signals = cleanSignals()
                    .timeSincePreviousRequest(100L)
                    .build();
            assertFalse(classifier.classify(signals).getTriggeredFeatures()
                    .containsKey("fast_requests"));
        }

        @Test
        @DisplayName("timeSincePreviousRequest > 100ms is NOT flagged")
        void slowRequestNotFlagged() {
            RawRequestSignals signals = cleanSignals()
                    .timeSincePreviousRequest(500L)
                    .build();
            assertFalse(classifier.classify(signals).getTriggeredFeatures()
                    .containsKey("fast_requests"));
        }

        @Test
        @DisplayName("Null timeSincePreviousRequest (first request) is NOT flagged")
        void nullTimingNotFlagged() {
            RawRequestSignals signals = cleanSignals()
                    .timeSincePreviousRequest(null)
                    .build();
            assertFalse(classifier.classify(signals).getTriggeredFeatures()
                    .containsKey("fast_requests"));
        }

        @Test
        @DisplayName("Fast request explanation mentions 'fast'")
        void fastRequestExplanation() {
            RawRequestSignals signals = cleanSignals()
                    .timeSincePreviousRequest(10L)
                    .build();
            String explanation = classifier.classify(signals).getExplanation().toLowerCase();
            assertTrue(explanation.contains("fast"), "Expected 'fast' in: " + explanation);
        }
    }

    // ── JavaScript disabled (Rule 7: +15 points) ────────────────────────────────

    @Nested
    @DisplayName("Rule 7 — JavaScript disabled")
    class JavaScriptDisabled {

        @Test
        @DisplayName("javascriptEnabled=false → 'javascript_disabled' feature")
        void jsDisabledAddsFeature() {
            RawRequestSignals signals = cleanSignals().javascriptEnabled(false).build();
            assertTrue(classifier.classify(signals).getTriggeredFeatures()
                    .containsKey("javascript_disabled"));
        }

        @Test
        @DisplayName("javascriptEnabled=true does NOT add feature")
        void jsEnabledNoFeature() {
            RawRequestSignals signals = cleanSignals().javascriptEnabled(true).build();
            assertFalse(classifier.classify(signals).getTriggeredFeatures()
                    .containsKey("javascript_disabled"));
        }

        @Test
        @DisplayName("javascriptEnabled=null (unknown) does NOT add feature")
        void jsNullNoFeature() {
            RawRequestSignals signals = cleanSignals().javascriptEnabled(null).build();
            assertFalse(classifier.classify(signals).getTriggeredFeatures()
                    .containsKey("javascript_disabled"));
        }
    }

    // ── Missing Referer (Rule 5: +5 points) ────────────────────────────────────

    @Nested
    @DisplayName("Rule 5 — Missing Referer")
    class MissingReferer {

        @Test
        @DisplayName("No referer on a non-first request → 'missing_referer' feature")
        void missingRefererOnNonFirstRequest() {
            // timeSincePreviousRequest != null means it's not the first request
            RawRequestSignals signals = cleanSignals()
                    .referer(null)
                    .timeSincePreviousRequest(500L)
                    .build();
            assertTrue(classifier.classify(signals).getTriggeredFeatures()
                    .containsKey("missing_referer"));
        }

        @Test
        @DisplayName("No referer on first request (null timing) does NOT add feature")
        void missingRefererFirstRequest() {
            RawRequestSignals signals = cleanSignals()
                    .referer(null)
                    .timeSincePreviousRequest(null)
                    .build();
            assertFalse(classifier.classify(signals).getTriggeredFeatures()
                    .containsKey("missing_referer"));
        }
    }

    // ── Severity thresholds ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("Severity thresholds")
    class SeverityThresholds {

        @Test
        @DisplayName("Score < 30 → Severity.LOW")
        void scoreBelowThirtyIsLow() {
            // Missing accept (15) + missing accept-lang (10) = 25 → LOW
            RawRequestSignals signals = cleanSignals()
                    .acceptHeader(null)
                    .acceptLanguage(null)
                    .build();
            assertEquals(Severity.LOW, classifier.classify(signals).getSeverity());
        }

        @Test
        @DisplayName("Score 30–49 → Severity.MEDIUM, isThreat=true")
        void scoreThirtyToFortyNineMedium() {
            // Missing UA (25) + missing accept-lang (10) = 35 → MEDIUM
            RawRequestSignals signals = cleanSignals()
                    .userAgent(null)
                    .acceptLanguage(null)
                    .build();
            ClassificationResult result = classifier.classify(signals);
            assertEquals(Severity.MEDIUM, result.getSeverity());
            assertTrue(result.isThreat());
        }

        @Test
        @DisplayName("Score 50–69 → Severity.HIGH")
        void scoreFiftyToSixtyNineHigh() {
            // Canary (50) alone → HIGH
            RawRequestSignals signals = cleanSignals().canaryTrapTriggered(true).build();
            assertEquals(Severity.HIGH, classifier.classify(signals).getSeverity());
        }

        @Test
        @DisplayName("Score >= 70 → Severity.CRITICAL")
        void scoreSeventyPlusCritical() {
            // Canary (50) + missing UA (25) = 75 → CRITICAL
            RawRequestSignals signals = cleanSignals()
                    .canaryTrapTriggered(true)
                    .userAgent(null)
                    .build();
            assertEquals(Severity.CRITICAL, classifier.classify(signals).getSeverity());
        }
    }

    // ── Confidence capping ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("Confidence capping")
    class ConfidenceCapping {

        @Test
        @DisplayName("Confidence never exceeds 1.0, even for very high scores")
        void confidenceCappedAtOne() {
            RawRequestSignals signals = RawRequestSignals.builder()
                    .sessionId("s")
                    .ipAddress("1.2.3.4")
                    .method("GET")
                    .uri("/admin")
                    .userAgent("Nikto/2.1") // scanner (+40)
                    .acceptHeader(null)     // +15
                    .acceptLanguage(null)   // +10
                    .canaryTrapTriggered(true) // +50
                    .timeSincePreviousRequest(5L) // +20
                    .javascriptEnabled(false) // +15
                    .timestamp(Instant.now())
                    .build();
            ClassificationResult result = classifier.classify(signals);
            assertTrue(result.getConfidence() <= 1.0,
                    "Confidence must not exceed 1.0, got: " + result.getConfidence());
        }

        @Test
        @DisplayName("anomalyScore equals confidence (both derived from threatScore/100)")
        void anomalyScoreMatchesConfidence() {
            RawRequestSignals signals = cleanSignals().canaryTrapTriggered(true).build();
            ClassificationResult result = classifier.classify(signals);
            assertEquals(result.getConfidence(), result.getAnomalyScore(), 0.0001);
        }
    }

    // ── Combined signal scenarios ────────────────────────────────────────────────

    @Nested
    @DisplayName("Combined signal scenarios")
    class CombinedScenarios {

        @Test
        @DisplayName("Multiple signals produce higher confidence than single signal")
        void multipleSignalsHigherConfidence() {
            RawRequestSignals single = cleanSignals()
                    .userAgent("GPT-Bot/1.0") // +30
                    .build();

            RawRequestSignals multiple = cleanSignals()
                    .userAgent("GPT-Bot/1.0") // +30
                    .canaryTrapTriggered(true) // +50
                    .build();

            double singleConfidence = classifier.classify(single).getConfidence();
            double multipleConfidence = classifier.classify(multiple).getConfidence();
            assertTrue(multipleConfidence > singleConfidence);
        }

        @Test
        @DisplayName("AI agent hitting canary trap → CRITICAL, AI_AGENT type")
        void aiAgentOnCanary() {
            RawRequestSignals signals = cleanSignals()
                    .userAgent("openai-crawler/1.0") // AI +30
                    .canaryTrapTriggered(true) // +50
                    .build();
            ClassificationResult result = classifier.classify(signals);
            assertEquals(ClientType.AI_AGENT, result.getClientType());
            assertEquals(Severity.CRITICAL, result.getSeverity()); // 80 pts
        }

        @Test
        @DisplayName("Security scanner hitting canary trap → CRITICAL, SECURITY_SCANNER type")
        void securityScannerOnCanary() {
            RawRequestSignals signals = cleanSignals()
                    .userAgent("Nikto/2.1.6") // scanner +40
                    .canaryTrapTriggered(true) // +50
                    .build();
            ClassificationResult result = classifier.classify(signals);
            assertEquals(ClientType.SECURITY_SCANNER, result.getClientType());
            assertEquals(Severity.CRITICAL, result.getSeverity()); // 90 pts
        }

        @Test
        @DisplayName("Explanation includes all triggered rule descriptions")
        void explanationCoversAllTriggeredRules() {
            RawRequestSignals signals = cleanSignals()
                    .canaryTrapTriggered(true)
                    .userAgent("GPT-Bot/1.0")
                    .acceptHeader(null)
                    .build();
            String explanation = classifier.classify(signals).getExplanation().toLowerCase();
            assertTrue(explanation.contains("canary"), "Missing canary in: " + explanation);
            assertTrue(explanation.contains("ai agent"), "Missing AI agent in: " + explanation);
            assertTrue(explanation.contains("accept"), "Missing accept in: " + explanation);
        }
    }

    // ── clientType fallback logic ────────────────────────────────────────────────

    @Nested
    @DisplayName("ClientType fallback logic")
    class ClientTypeFallback {

        @Test
        @DisplayName("Score >= 40 with no explicit UA type → BOT_SCRAPER")
        void highScoreNoUaTypeFallsToBotScraper() {
            // Missing UA (25) + no accept (15) = 40 → BOT_SCRAPER fallback
            RawRequestSignals signals = cleanSignals()
                    .userAgent(null)    // +25, sets UNKNOWN initially
                    .acceptHeader(null) // +15
                    .build();
            ClassificationResult result = classifier.classify(signals);
            assertEquals(ClientType.BOT_SCRAPER, result.getClientType());
        }

        @Test
        @DisplayName("Score < 20 with no UA signals → HUMAN_BROWSER")
        void lowScoreFallsToHumanBrowser() {
            // Only missing referer on non-first request (+5) = 5 → HUMAN_BROWSER
            RawRequestSignals signals = cleanSignals()
                    .referer(null)
                    .timeSincePreviousRequest(500L)
                    .build();
            assertEquals(ClientType.HUMAN_BROWSER, classifier.classify(signals).getClientType());
        }
    }
}