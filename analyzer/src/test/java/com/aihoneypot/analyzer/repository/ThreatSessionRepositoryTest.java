package com.aihoneypot.analyzer.repository;

import com.aihoneypot.analyzer.entity.ThreatSession;
import com.aihoneypot.core.model.ClientType;
import com.aihoneypot.core.model.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ThreatSessionRepository Tests")
class ThreatSessionRepositoryTest {

    @Autowired
    private ThreatSessionRepository repository;

    private ThreatSession threatSession1;
    private ThreatSession threatSession2;
    private ThreatSession threatSession3;

    @BeforeEach
    void setUp() {
        Instant now = Instant.now();

        threatSession1 = ThreatSession.builder()
                .sessionId("session-001")
                .ipAddress("192.168.1.100")
                .timestamp(now)
                .clientType(ClientType.BOT_SCRAPER)
                .confidence(0.95)
                .severity(Severity.CRITICAL)
                .isThreat(true)
                .anomalyScore(0.88)
                .userAgent("Mozilla/5.0 Bot")
                .firstUri("/admin")
                .requestCount(5)
                .explanation("Suspicious bot activity detected")
                .classifierName("BotClassifier")
                .canaryTrapTriggered(true)
                .build();

        threatSession2 = ThreatSession.builder()
                .sessionId("session-002")
                .ipAddress("10.0.0.50")
                .timestamp(now.minusSeconds(3600))
                .clientType(ClientType.SECURITY_SCANNER)
                .confidence(0.85)
                .severity(Severity.HIGH)
                .isThreat(true)
                .anomalyScore(0.75)
                .userAgent("Nmap Scanner")
                .firstUri("/robots.txt")
                .requestCount(12)
                .explanation("Port scanner detected")
                .classifierName("ScannerClassifier")
                .canaryTrapTriggered(false)
                .build();

        threatSession3 = ThreatSession.builder()
                .sessionId("session-003")
                .ipAddress("192.168.1.100")
                .timestamp(now.minusSeconds(7200))
                .clientType(ClientType.UNKNOWN)
                .confidence(0.60)
                .severity(Severity.MEDIUM)
                .isThreat(false)
                .anomalyScore(0.45)
                .userAgent("Mozilla/5.0")
                .firstUri("/login")
                .requestCount(2)
                .explanation("Unusual pattern detected")
                .classifierName("AnomalyClassifier")
                .canaryTrapTriggered(false)
                .build();

        repository.saveAll(List.of(threatSession1, threatSession2, threatSession3));
    }

    @Test
    @DisplayName("Should find threat session by session ID")
    void testFindBySessionId() {
        Optional<ThreatSession> found = repository.findBySessionId("session-001");

        assertThat(found).isPresent();
        assertThat(found.get().getIpAddress()).isEqualTo("192.168.1.100");
        assertThat(found.get().getSeverity()).isEqualTo(Severity.CRITICAL);
    }

    @Test
    @DisplayName("Should return empty when session ID not found")
    void testFindBySessionIdNotFound() {
        Optional<ThreatSession> found = repository.findBySessionId("non-existent");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find all threats from specific IP address")
    void testFindByIpAddress() {
        List<ThreatSession> threats = repository.findByIpAddress("192.168.1.100");

        assertThat(threats).hasSize(2);
        assertThat(threats).extracting(ThreatSession::getSessionId)
                .containsExactlyInAnyOrder("session-001", "session-003");
    }

    @Test
    @DisplayName("Should return empty list for IP with no threats")
    void testFindByIpAddressNotFound() {
        List<ThreatSession> threats = repository.findByIpAddress("255.255.255.255");

        assertThat(threats).isEmpty();
    }

    @Test
    @DisplayName("Should find all threats by client type")
    void testFindByClientType() {
        List<ThreatSession> botThreats = repository.findByClientType(ClientType.BOT_SCRAPER);

        assertThat(botThreats).hasSize(1);
        assertThat(botThreats.get(0).getSessionId()).isEqualTo("session-001");
    }

    @Test
    @DisplayName("Should find all threats by severity")
    void testFindBySeverity() {
        List<ThreatSession> criticalThreats = repository.findBySeverity(Severity.CRITICAL);

        assertThat(criticalThreats).hasSize(1);
        assertThat(criticalThreats.get(0).getSeverity()).isEqualTo(Severity.CRITICAL);
    }

    @Test
    @DisplayName("Should find all confirmed threats")
    void testFindByIsThreatTrue() {
        List<ThreatSession> confirmedThreats = repository.findByIsThreatTrue();

        assertThat(confirmedThreats).hasSize(2);
        assertThat(confirmedThreats).extracting(ThreatSession::getIsThreat)
                .containsOnly(true);
    }

    @Test
    @DisplayName("Should find threats within time range")
    void testFindByTimestampBetween() {
        Instant now = Instant.now();
        Instant oneHourAgo = now.minusSeconds(3600);
        Instant nineHoursAgo = now.minusSeconds(32400);

        List<ThreatSession> threatsInRange = repository.findByTimestampBetween(nineHoursAgo, now);

        assertThat(threatsInRange).hasSize(3);
    }

    @Test
    @DisplayName("Should find threats that triggered canary traps")
    void testFindByCanaryTrapTriggeredTrue() {
        List<ThreatSession> canaryThreats = repository.findByCanaryTrapTriggeredTrue();

        assertThat(canaryThreats).hasSize(1);
        assertThat(canaryThreats.get(0).getSessionId()).isEqualTo("session-001");
    }

    @Test
    @DisplayName("Should count threats by client type")
    void testCountByClientType() {
        long botCount = repository.countByClientType(ClientType.BOT_SCRAPER);
        long scannerCount = repository.countByClientType(ClientType.SECURITY_SCANNER);

        assertThat(botCount).isEqualTo(1);
        assertThat(scannerCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Should count total confirmed threats")
    void testCountByIsThreatTrue() {
        long confirmedCount = repository.countByIsThreatTrue();

        assertThat(confirmedCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Should get top 100 recent threats ordered by timestamp")
    void testFindTop100ByOrderByTimestampDesc() {
        List<ThreatSession> recentThreats = repository.findTop100ByOrderByTimestampDesc();

        assertThat(recentThreats).hasSize(3);
        assertThat(recentThreats.get(0).getSessionId()).isEqualTo("session-001");
    }

    @Test
    @DisplayName("Should count threats by severity")
    void testCountBySeverity() {
        List<Object[]> results = repository.countBySeverity();

        assertThat(results).isNotEmpty();
        assertThat(results).anySatisfy(row -> {
            assertThat(row[0]).isEqualTo(Severity.CRITICAL);
            assertThat(row[1]).isEqualTo(1L);
        });
    }

    @Test
    @DisplayName("Should count threats by client type grouped")
    void testCountByClientTypeGrouped() {
        List<Object[]> results = repository.countByClientTypeGrouped();

        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(3);
    }

    @Test
    @DisplayName("Should get top attacking IPs")
    void testFindTopAttackingIps() {
        List<Object[]> topIps = repository.findTopAttackingIps();

        assertThat(topIps).isNotEmpty();
        assertThat(topIps).extracting(row -> row[0])
                .contains("192.168.1.100", "10.0.0.50");
    }


    @Test
    @DisplayName("Should save and retrieve threat session")
    void testSaveAndRetrieve() {
        ThreatSession newThreat = ThreatSession.builder()
                .sessionId("session-new")
                .ipAddress("172.16.0.1")
                .timestamp(Instant.now())
                .clientType(ClientType.UNKNOWN)
                .confidence(0.70)
                .severity(Severity.HIGH)
                .isThreat(true)
                .anomalyScore(0.65)
                .userAgent("Unknown")
                .firstUri("/api/users")
                .requestCount(3)
                .explanation("Test threat")
                .classifierName("TestClassifier")
                .canaryTrapTriggered(false)
                .build();

        ThreatSession saved = repository.save(newThreat);

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findBySessionId("session-new")).isPresent();
    }
}
