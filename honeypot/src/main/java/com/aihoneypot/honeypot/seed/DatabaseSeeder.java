package com.aihoneypot.honeypot.seed;

import com.aihoneypot.analyzer.entity.ThreatSession;
import com.aihoneypot.analyzer.repository.ThreatSessionRepository;
import com.aihoneypot.core.model.ClientType;
import com.aihoneypot.core.model.Severity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Data Seeder - Populates database with initial realistic data for demonstration.
 * Only runs in dev/demo profiles.
 */
@Slf4j
@Component
@Profile({"dev", "demo", "default"})
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final ThreatSessionRepository threatRepository;

    @Override
    public void run(String... args) {
        if (threatRepository.count() > 0) {
            log.info("📊 Database already contains data. Skipping seed.");
            return;
        }

        log.info("🌱 Seeding database with initial data...");

        List<ThreatSession> sessions = new ArrayList<>();

        // Generate historical data for the last 7 days
        Instant now = Instant.now();

        // Day 7 ago - Low activity
        sessions.addAll(generateDayData(now.minus(7, ChronoUnit.DAYS), 15));

        // Day 6 ago - Medium activity
        sessions.addAll(generateDayData(now.minus(6, ChronoUnit.DAYS), 25));

        // Day 5 ago - High activity (attack wave)
        sessions.addAll(generateDayData(now.minus(5, ChronoUnit.DAYS), 50));

        // Day 4 ago - Medium activity
        sessions.addAll(generateDayData(now.minus(4, ChronoUnit.DAYS), 30));

        // Day 3 ago - Low activity
        sessions.addAll(generateDayData(now.minus(3, ChronoUnit.DAYS), 20));

        // Day 2 ago - Medium activity with SQL injection spike
        sessions.addAll(generateDayData(now.minus(2, ChronoUnit.DAYS), 35));
        sessions.addAll(generateSQLInjectionSpike(now.minus(2, ChronoUnit.DAYS), 10));

        // Yesterday - High activity (bot scan)
        sessions.addAll(generateDayData(now.minus(1, ChronoUnit.DAYS), 45));
        sessions.addAll(generateBotScanSpike(now.minus(1, ChronoUnit.DAYS), 15));

        // Today - Current activity
        sessions.addAll(generateDayData(now, 25));

        // Save all
        threatRepository.saveAll(sessions);

        log.info("✅ Database seeded with {} threat sessions", sessions.size());
        log.info("📊 Data distribution:");
        log.info("   - Last 7 days: {}", sessions.size());
        log.info("   - Critical: {}", sessions.stream().filter(s -> s.getSeverity() == Severity.CRITICAL).count());
        log.info("   - High: {}", sessions.stream().filter(s -> s.getSeverity() == Severity.HIGH).count());
        log.info("   - Medium: {}", sessions.stream().filter(s -> s.getSeverity() == Severity.MEDIUM).count());
        log.info("   - Low: {}", sessions.stream().filter(s -> s.getSeverity() == Severity.LOW).count());
    }

    private List<ThreatSession> generateDayData(Instant baseTime, int count) {
        List<ThreatSession> sessions = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            // Spread requests throughout the day
            Instant timestamp = baseTime.plus(
                ThreadLocalRandom.current().nextInt(0, 86400),
                ChronoUnit.SECONDS
            );

            sessions.add(generateRandomThreat(timestamp));
        }

        return sessions;
    }

    private ThreatSession generateRandomThreat(Instant timestamp) {
        double random = Math.random();

        if (random < 0.5) {
            // 50% legitimate/low severity
            return createThreat(timestamp, ClientType.HUMAN_BROWSER, Severity.LOW, "192.168.1." + randomInt(1, 254));
        } else if (random < 0.7) {
            // 20% bots
            return createThreat(timestamp, ClientType.SEARCH_ENGINE, Severity.LOW, "198.51.100." + randomInt(1, 254));
        } else if (random < 0.85) {
            // 15% AI agents
            return createThreat(timestamp, ClientType.AI_AGENT, Severity.MEDIUM, "203.0.113." + randomInt(1, 254));
        } else if (random < 0.95) {
            // 10% bot scrapers
            return createThreat(timestamp, ClientType.BOT_SCRAPER, Severity.HIGH, "192.0.2." + randomInt(1, 254));
        } else {
            // 5% critical threats
            return createThreat(timestamp, ClientType.SECURITY_SCANNER, Severity.CRITICAL, "10.0.0." + randomInt(1, 254));
        }
    }

    private List<ThreatSession> generateSQLInjectionSpike(Instant baseTime, int count) {
        List<ThreatSession> sessions = new ArrayList<>();
        String attackerIP = "192.0.2." + randomInt(100, 200);

        for (int i = 0; i < count; i++) {
            Instant timestamp = baseTime.plus(i * 5, ChronoUnit.SECONDS);

            ThreatSession session = createThreat(
                timestamp,
                ClientType.SECURITY_SCANNER,
                Severity.CRITICAL,
                attackerIP
            );
            session.setExplanation("SQL injection attempt detected: ' OR '1'='1");
            sessions.add(session);
        }

        return sessions;
    }

    private List<ThreatSession> generateBotScanSpike(Instant baseTime, int count) {
        List<ThreatSession> sessions = new ArrayList<>();
        String botIP = "198.51.100." + randomInt(50, 100);

        for (int i = 0; i < count; i++) {
            Instant timestamp = baseTime.plus(i * 2, ChronoUnit.SECONDS);

            ThreatSession session = createThreat(
                timestamp,
                ClientType.BOT_SCRAPER,
                Severity.HIGH,
                botIP
            );
            session.setExplanation("Automated bot scanning detected");
            sessions.add(session);
        }

        return sessions;
    }

    private ThreatSession createThreat(Instant timestamp, ClientType type, Severity severity, String ip) {
        ThreatSession session = new ThreatSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setIpAddress(ip);
        session.setClientType(type);
        session.setSeverity(severity);
        session.setConfidence(0.7 + (Math.random() * 0.3)); // 0.7 - 1.0
        session.setExplanation(generateExplanation(type, severity));
        session.setTimestamp(timestamp);
        session.setRequestCount(randomInt(1, 5));
        session.setIsThreat(severity == Severity.HIGH || severity == Severity.CRITICAL);

        return session;
    }

    private String generateExplanation(ClientType type, Severity severity) {
        switch (type) {
            case HUMAN_BROWSER:
                return "Legitimate browser traffic detected";
            case SEARCH_ENGINE:
                return "Search engine bot detected (Googlebot/Bingbot)";
            case AI_AGENT:
                return "AI agent detected - GPT/Claude Bot";
            case BOT_SCRAPER:
                return "Automated scraping bot detected";
            case SECURITY_SCANNER:
                if (severity == Severity.CRITICAL) {
                    return "Critical threat: SQL injection or XSS attempt";
                }
                return "Malicious scanning activity detected";
            default:
                return "Unknown client type";
        }
    }

    private int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}

