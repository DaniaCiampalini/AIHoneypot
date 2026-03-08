package com.aihoneypot.honeypot.simulator;

import com.aihoneypot.core.model.ClientType;
import com.aihoneypot.core.model.RawRequestSignals;
import com.aihoneypot.core.model.Severity;
import com.aihoneypot.core.interfaces.ThreatClassifier;
import com.aihoneypot.analyzer.service.ThreatSessionRepository;
import com.aihoneypot.analyzer.model.ThreatSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Advanced Traffic Simulator - Generates realistic honeypot traffic for demonstration.
 * Simulates various attack patterns, legitimate traffic, and AI/bot behaviors.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrafficSimulator {

    private final ThreatClassifier classifier;
    private final ThreatSessionRepository threatRepository;

    private static final String[] LEGITIMATE_IPS = {
        "203.0.113.1", "203.0.113.45", "203.0.113.89", "203.0.113.123"
    };

    private static final String[] BOT_IPS = {
        "198.51.100.10", "198.51.100.20", "198.51.100.30", "198.51.100.40"
    };

    private static final String[] MALICIOUS_IPS = {
        "192.0.2.50", "192.0.2.75", "192.0.2.100", "192.0.2.125"
    };

    private static final String[] LEGITIMATE_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Safari/605.1.15"
    };

    private static final String[] BOT_AGENTS = {
        "Googlebot/2.1 (+http://www.google.com/bot.html)",
        "Mozilla/5.0 (compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm)",
        "curl/7.68.0",
        "Python-urllib/3.10",
        "Go-http-client/1.1"
    };

    private static final String[] AI_AGENTS = {
        "GPT-Bot/1.0 (+https://openai.com/gptbot)",
        "ClaudeBot/1.0 (+https://anthropic.com/claudebot)",
        "ChatGPT-User/1.0",
        "AI-Assistant/2.0"
    };

    private static final String[] MALICIOUS_AGENTS = {
        "sqlmap/1.7.2#stable (http://sqlmap.org)",
        "Nikto/2.1.6",
        "Nmap Scripting Engine",
        "masscan/1.0"
    };

    private static final String[] LEGITIMATE_PATHS = {
        "/", "/home", "/about", "/contact", "/products", "/services", "/blog", "/pricing"
    };

    private static final String[] CANARY_PATHS = {
        "/admin", "/wp-admin", "/.env", "/backup", "/.git", "/config.php",
        "/phpmyadmin", "/api/internal", "/.aws/credentials", "/database.sql"
    };

    private static final String[] SCAN_PATHS = {
        "/admin/login", "/manager/html", "/cgi-bin/test.cgi", "/shell.php",
        "/upload.php", "/eval.php", "/cmd.php", "/backdoor.php"
    };

    /**
     * Generates continuous realistic traffic every 5 seconds
     */
    @Scheduled(fixedRate = 5000)
    public void generateRealisticTraffic() {
        try {
            int numRequests = ThreadLocalRandom.current().nextInt(1, 4);

            for (int i = 0; i < numRequests; i++) {
                generateRandomRequest();
            }
        } catch (Exception e) {
            log.error("Error generating traffic: {}", e.getMessage());
        }
    }

    /**
     * Generates attack simulation every 30 seconds
     */
    @Scheduled(fixedRate = 30000)
    public void generateAttackSimulation() {
        try {
            double random = Math.random();

            if (random < 0.3) {
                simulateCanaryTrapAccess();
            } else if (random < 0.5) {
                simulateBotScan();
            } else if (random < 0.7) {
                simulateAIAgentProbing();
            } else {
                simulateSQLInjectionAttempt();
            }
        } catch (Exception e) {
            log.error("Error generating attack: {}", e.getMessage());
        }
    }

    /**
     * Generates burst traffic every 2 minutes (simulates attack wave)
     */
    @Scheduled(fixedRate = 120000)
    public void generateBurstTraffic() {
        try {
            log.info("🌊 Generating burst traffic wave...");
            int burstSize = ThreadLocalRandom.current().nextInt(5, 15);

            for (int i = 0; i < burstSize; i++) {
                if (Math.random() < 0.7) {
                    simulateCanaryTrapAccess();
                } else {
                    simulateBotScan();
                }
                Thread.sleep(100); // Small delay between requests
            }

            log.info("✅ Burst traffic wave completed: {} requests", burstSize);
        } catch (Exception e) {
            log.error("Error generating burst: {}", e.getMessage());
        }
    }

    private void generateRandomRequest() {
        double random = Math.random();

        if (random < 0.6) {
            // 60% legitimate traffic
            generateLegitimateRequest();
        } else if (random < 0.8) {
            // 20% bot traffic
            generateBotRequest();
        } else if (random < 0.9) {
            // 10% AI agent
            generateAIRequest();
        } else {
            // 10% malicious
            generateMaliciousRequest();
        }
    }

    private void generateLegitimateRequest() {
        RawRequestSignals signals = RawRequestSignals.builder()
                .sessionId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .ipAddress(randomFrom(LEGITIMATE_IPS))
                .method("GET")
                .uri(randomFrom(LEGITIMATE_PATHS))
                .userAgent(randomFrom(LEGITIMATE_AGENTS))
                .headers(createHeaders("text/html,application/xhtml+xml", "en-US,en;q=0.9"))
                .build();

        processAndSave(signals, ClientType.HUMAN_BROWSER, Severity.INFO);
    }

    private void generateBotRequest() {
        RawRequestSignals signals = RawRequestSignals.builder()
                .sessionId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .ipAddress(randomFrom(BOT_IPS))
                .method("GET")
                .uri(randomFrom(LEGITIMATE_PATHS))
                .userAgent(randomFrom(BOT_AGENTS))
                .headers(createHeaders("*/*", "en-US"))
                .build();

        processAndSave(signals, ClientType.BOT_CRAWLER, Severity.LOW);
    }

    private void generateAIRequest() {
        RawRequestSignals signals = RawRequestSignals.builder()
                .sessionId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .ipAddress(randomFrom(BOT_IPS))
                .method("GET")
                .uri(randomFrom(LEGITIMATE_PATHS))
                .userAgent(randomFrom(AI_AGENTS))
                .headers(createHeaders("application/json", "en"))
                .build();

        processAndSave(signals, ClientType.AI_AGENT, Severity.MEDIUM);
    }

    private void generateMaliciousRequest() {
        RawRequestSignals signals = RawRequestSignals.builder()
                .sessionId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .ipAddress(randomFrom(MALICIOUS_IPS))
                .method("GET")
                .uri(randomFrom(SCAN_PATHS))
                .userAgent(randomFrom(MALICIOUS_AGENTS))
                .headers(createHeaders("*/*", "en"))
                .build();

        processAndSave(signals, ClientType.MALICIOUS_SCANNER, Severity.HIGH);
    }

    private void simulateCanaryTrapAccess() {
        String maliciousIP = randomFrom(MALICIOUS_IPS);
        String canaryPath = randomFrom(CANARY_PATHS);

        RawRequestSignals signals = RawRequestSignals.builder()
                .sessionId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .ipAddress(maliciousIP)
                .method(Math.random() < 0.7 ? "GET" : "POST")
                .uri(canaryPath)
                .userAgent(randomFrom(BOT_AGENTS))
                .headers(createHeaders("*/*", "en"))
                .build();

        log.warn("🚨 Canary trap accessed: {} by {}", canaryPath, maliciousIP);
        processAndSave(signals, ClientType.MALICIOUS_SCANNER, Severity.CRITICAL);
    }

    private void simulateBotScan() {
        String scanIP = randomFrom(MALICIOUS_IPS);

        RawRequestSignals signals = RawRequestSignals.builder()
                .sessionId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .ipAddress(scanIP)
                .method("GET")
                .uri(randomFrom(SCAN_PATHS))
                .userAgent(randomFrom(MALICIOUS_AGENTS))
                .headers(createHeaders("*/*", null))
                .build();

        log.warn("🤖 Bot scan detected from: {}", scanIP);
        processAndSave(signals, ClientType.BOT_SCRAPER, Severity.HIGH);
    }

    private void simulateAIAgentProbing() {
        RawRequestSignals signals = RawRequestSignals.builder()
                .sessionId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .ipAddress(randomFrom(BOT_IPS))
                .method("GET")
                .uri(randomFrom(LEGITIMATE_PATHS))
                .userAgent(randomFrom(AI_AGENTS))
                .headers(createHeaders("application/json", "en"))
                .build();

        log.info("🤖 AI agent probing detected");
        processAndSave(signals, ClientType.AI_AGENT, Severity.MEDIUM);
    }

    private void simulateSQLInjectionAttempt() {
        Map<String, String> maliciousParams = new HashMap<>();
        maliciousParams.put("id", "1' OR '1'='1");
        maliciousParams.put("user", "admin'--");

        RawRequestSignals signals = RawRequestSignals.builder()
                .sessionId(UUID.randomUUID().toString())
                .timestamp(Instant.now())
                .ipAddress(randomFrom(MALICIOUS_IPS))
                .method("GET")
                .uri("/api/users")
                .userAgent(randomFrom(MALICIOUS_AGENTS))
                .queryParams(maliciousParams)
                .headers(createHeaders("*/*", "en"))
                .build();

        log.error("💉 SQL injection attempt detected!");
        processAndSave(signals, ClientType.MALICIOUS_SCANNER, Severity.CRITICAL);
    }

    private void processAndSave(RawRequestSignals signals, ClientType type, Severity severity) {
        try {
            // Classify the request
            var result = classifier.classify(signals);

            // Create threat session
            ThreatSession session = new ThreatSession();
            session.setSessionId(signals.getSessionId());
            session.setIpAddress(signals.getIpAddress());
            session.setClientType(type);
            session.setSeverity(severity);
            session.setConfidence(result.getConfidence());
            session.setExplanation(result.getExplanation());
            session.setFirstSeen(signals.getTimestamp());
            session.setLastSeen(signals.getTimestamp());
            session.setRequestCount(1);

            // Save to database
            threatRepository.save(session);

        } catch (Exception e) {
            log.error("Error processing request: {}", e.getMessage());
        }
    }

    private Map<String, String> createHeaders(String accept, String language) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", accept);
        if (language != null) {
            headers.put("Accept-Language", language);
        }
        headers.put("Connection", "keep-alive");
        return headers;
    }

    private <T> T randomFrom(T[] array) {
        return array[ThreadLocalRandom.current().nextInt(array.length)];
    }
}

