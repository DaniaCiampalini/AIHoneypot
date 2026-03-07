package com.aihoneypot.analyzer.classification;

import com.aihoneypot.core.interfaces.ThreatClassifier;
import com.aihoneypot.core.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Rule-based classifier using heuristics and pattern matching.
 * This is the fastest classifier and serves as a baseline.
 */
@Slf4j
@Component
public class RuleBasedClassifier implements ThreatClassifier {

    private static final String CLASSIFIER_NAME = "RuleBased";

    @Override
    public ClassificationResult classify(RawRequestSignals signals) {
        Map<String, Object> triggeredFeatures = new HashMap<>();
        int threatScore = 0;
        ClientType detectedType = ClientType.UNKNOWN;
        StringBuilder explanation = new StringBuilder();

        // Rule 1: Canary trap triggered = HIGH threat
        if (signals.isCanaryTrapTriggered()) {
            threatScore += 50;
            triggeredFeatures.put("canary_trap", true);
            explanation.append("Canary trap triggered. ");
        }

        // Rule 2: Missing Accept header (common in bots)
        if (signals.getAcceptHeader() == null || signals.getAcceptHeader().isEmpty()) {
            threatScore += 15;
            triggeredFeatures.put("missing_accept_header", true);
            explanation.append("Missing Accept header. ");
        }

        // Rule 3: Missing Accept-Language (common in bots)
        if (signals.getAcceptLanguage() == null || signals.getAcceptLanguage().isEmpty()) {
            threatScore += 10;
            triggeredFeatures.put("missing_accept_language", true);
            explanation.append("Missing Accept-Language header. ");
        }

        // Rule 4: User-Agent analysis
        String ua = signals.getUserAgent();
        if (ua != null) {
            String uaLower = ua.toLowerCase();

            // Known bot patterns
            if (uaLower.contains("bot") || uaLower.contains("crawler") ||
                uaLower.contains("spider") || uaLower.contains("scraper")) {
                threatScore += 20;
                detectedType = ClientType.BOT_SCRAPER;
                triggeredFeatures.put("bot_user_agent", true);
                explanation.append("Bot-like User-Agent. ");
            }

            // AI agent patterns (GPT, Claude, etc.)
            if (uaLower.contains("gpt") || uaLower.contains("openai") ||
                uaLower.contains("claude") || uaLower.contains("anthropic") ||
                uaLower.contains("langchain") || uaLower.contains("llm")) {
                threatScore += 30;
                detectedType = ClientType.AI_AGENT;
                triggeredFeatures.put("ai_agent_user_agent", true);
                explanation.append("AI agent User-Agent detected. ");
            }

            // Security scanners
            if (uaLower.contains("nikto") || uaLower.contains("nmap") ||
                uaLower.contains("masscan") || uaLower.contains("burp")) {
                threatScore += 40;
                detectedType = ClientType.SECURITY_SCANNER;
                triggeredFeatures.put("security_scanner", true);
                explanation.append("Security scanner detected. ");
            }
        } else {
            // No User-Agent at all
            threatScore += 25;
            triggeredFeatures.put("missing_user_agent", true);
            explanation.append("Missing User-Agent. ");
        }

        // Rule 5: Missing Referer on non-initial requests
        if (signals.getReferer() == null && signals.getTimeSincePreviousRequest() != null) {
            threatScore += 5;
            triggeredFeatures.put("missing_referer", true);
            explanation.append("Missing Referer. ");
        }

        // Rule 6: Very fast requests (< 100ms between requests)
        if (signals.getTimeSincePreviousRequest() != null &&
            signals.getTimeSincePreviousRequest() < 100) {
            threatScore += 20;
            triggeredFeatures.put("fast_requests", signals.getTimeSincePreviousRequest());
            explanation.append("Suspiciously fast requests. ");
        }

        // Rule 7: JavaScript disabled (if detectable)
        if (Boolean.FALSE.equals(signals.getJavascriptEnabled())) {
            threatScore += 15;
            triggeredFeatures.put("javascript_disabled", true);
            explanation.append("JavaScript disabled. ");
        }

        // Determine client type if not already set
        if (detectedType == ClientType.UNKNOWN) {
            if (threatScore >= 40) {
                detectedType = ClientType.BOT_SCRAPER;
            } else if (threatScore >= 20) {
                detectedType = ClientType.UNKNOWN;
            } else {
                detectedType = ClientType.HUMAN_BROWSER;
            }
        }

        // Calculate confidence and severity
        double confidence = Math.min(threatScore / 100.0, 1.0);
        Severity severity = calculateSeverity(threatScore);
        boolean isThreat = threatScore >= 30;

        if (explanation.length() == 0) {
            explanation.append("Normal behavior detected.");
        }

        return ClassificationResult.builder()
            .sessionId(signals.getSessionId())
            .timestamp(Instant.now())
            .clientType(detectedType)
            .confidence(confidence)
            .severity(severity)
            .isThreat(isThreat)
            .anomalyScore(threatScore / 100.0)
            .explanation(explanation.toString().trim())
            .triggeredFeatures(triggeredFeatures)
            .classifierName(CLASSIFIER_NAME)
            .build();
    }

    private Severity calculateSeverity(int threatScore) {
        if (threatScore >= 70) return Severity.CRITICAL;
        if (threatScore >= 50) return Severity.HIGH;
        if (threatScore >= 30) return Severity.MEDIUM;
        return Severity.LOW;
    }

    @Override
    public String getName() {
        return CLASSIFIER_NAME;
    }

    @Override
    public boolean isReady() {
        return true;
    }
}

