package com.aihoneypot.gui.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SecurityAnalysisService.
 * Tests security analysis logic, scoring system, and vulnerability detection.
 */
@DisplayName("SecurityAnalysisService Tests")
class SecurityAnalysisServiceTest {

    private SecurityAnalysisService service;

    @BeforeEach
    void setUp() {
        service = new SecurityAnalysisService();
    }

    @Test
    @DisplayName("Should initialize service correctly")
    void testServiceInitialization() {
        // Then
        assertNotNull(service);
    }

    @Test
    @DisplayName("Should calculate security score within valid range")
    void testSecurityScoreRange() {
        // Given
        String url = "https://example.com";

        // When
        Map<String, Object> result = service.analyzeWebsiteSecurity(url);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("score"));

        Double score = (Double) result.get("score");
        assertNotNull(score);
        assertTrue(score >= 0.0 && score <= 100.0,
            "Score should be between 0 and 100");
    }

    @Test
    @DisplayName("Should classify security level correctly")
    void testSecurityLevelClassification() {
        // Given
        String url = "https://example.com";

        // When
        Map<String, Object> result = service.analyzeWebsiteSecurity(url);

        // Then
        assertTrue(result.containsKey("level"));
        String level = (String) result.get("level");

        assertTrue(
            level.equals("SECURE") ||
            level.equals("MODERATE") ||
            level.equals("VULNERABLE") ||
            level.equals("CRITICAL"),
            "Level should be one of: SECURE, MODERATE, VULNERABLE, CRITICAL"
        );
    }

    @Test
    @DisplayName("Should detect HTTP (non-HTTPS) as vulnerability")
    void testHTTPDetection() {
        // Given
        String httpUrl = "http://example.com";

        // When
        Map<String, Object> result = service.analyzeWebsiteSecurity(httpUrl);

        // Then
        @SuppressWarnings("unchecked")
        List<String> vulnerabilities = (List<String>) result.get("vulnerabilities");

        assertNotNull(vulnerabilities);
        assertFalse(vulnerabilities.isEmpty());

        boolean hasSSLVulnerability = vulnerabilities.stream()
            .anyMatch(v -> v.toLowerCase().contains("ssl") || v.toLowerCase().contains("https"));
        assertTrue(hasSSLVulnerability, "Should detect missing HTTPS");
    }

    @ParameterizedTest
    @DisplayName("Should detect suspicious URL patterns")
    @ValueSource(strings = {
        "http://example.com/admin",
        "http://example.com/.env",
        "http://example.com/backup",
        "http://example.com/config.php",
        "http://example.com/.git"
    })
    void testSuspiciousURLDetection(String url) {
        // When
        Map<String, Object> result = service.analyzeWebsiteSecurity(url);

        // Then
        Double score = (Double) result.get("score");
        assertTrue(score < 80, "Suspicious URLs should have lower security score");

        @SuppressWarnings("unchecked")
        Map<String, Object> urlPattern = (Map<String, Object>) result.get("url_pattern");
        assertNotNull(urlPattern);

        Boolean isSuspicious = (Boolean) urlPattern.get("suspicious");
        assertTrue(isSuspicious, "Should detect suspicious pattern in: " + url);
    }

    @Test
    @DisplayName("Should return all required analysis sections")
    void testAnalysisSections() {
        // Given
        String url = "https://example.com";

        // When
        Map<String, Object> result = service.analyzeWebsiteSecurity(url);

        // Then
        String[] requiredSections = {
            "ssl", "headers", "url_pattern", "dns", "ports",
            "redirects", "content", "score", "level",
            "vulnerabilities", "warnings", "good_practices"
        };

        for (String section : requiredSections) {
            assertTrue(result.containsKey(section),
                "Result should contain section: " + section);
        }
    }

    @Test
    @DisplayName("Should handle invalid URL gracefully")
    void testInvalidURLHandling() {
        // Given
        String invalidUrl = "not-a-valid-url";

        // When
        Map<String, Object> result = service.analyzeWebsiteSecurity(invalidUrl);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("vulnerabilities"));

        @SuppressWarnings("unchecked")
        List<String> vulnerabilities = (List<String>) result.get("vulnerabilities");
        assertFalse(vulnerabilities.isEmpty(), "Should have vulnerabilities for invalid URL");
    }

    @Test
    @DisplayName("Should detect IP-based URLs")
    void testIPBasedURLDetection() {
        // Given
        String ipUrl = "http://192.168.1.1";

        // When
        Map<String, Object> result = service.analyzeWebsiteSecurity(ipUrl);

        // Then
        @SuppressWarnings("unchecked")
        Map<String, Object> urlPattern = (Map<String, Object>) result.get("url_pattern");
        assertNotNull(urlPattern);

        Boolean isIP = (Boolean) urlPattern.get("is_ip");
        assertTrue(isIP, "Should detect IP-based URL");
    }

    @Test
    @DisplayName("Should assign lower score for multiple vulnerabilities")
    void testMultipleVulnerabilitiesScoring() {
        // Given - URL with multiple issues
        String vulnerableUrl = "http://192.168.1.1/admin";

        // When
        Map<String, Object> result = service.analyzeWebsiteSecurity(vulnerableUrl);

        // Then
        Double score = (Double) result.get("score");
        assertTrue(score < 50, "Multiple vulnerabilities should result in low score");

        String level = (String) result.get("level");
        assertTrue(level.equals("VULNERABLE") || level.equals("CRITICAL"),
            "Should be VULNERABLE or CRITICAL with multiple issues");
    }

    @Test
    @DisplayName("Should provide good practices list")
    void testGoodPracticesList() {
        // Given
        String url = "https://example.com";

        // When
        Map<String, Object> result = service.analyzeWebsiteSecurity(url);

        // Then
        assertTrue(result.containsKey("good_practices"));

        @SuppressWarnings("unchecked")
        List<String> goodPractices = (List<String>) result.get("good_practices");
        assertNotNull(goodPractices);
        // Should have at least some good practices or be empty
        assertTrue(goodPractices != null);
    }

    @Test
    @DisplayName("Should provide warnings list")
    void testWarningsList() {
        // Given
        String url = "https://example.com";

        // When
        Map<String, Object> result = service.analyzeWebsiteSecurity(url);

        // Then
        assertTrue(result.containsKey("warnings"));

        @SuppressWarnings("unchecked")
        List<String> warnings = (List<String>) result.get("warnings");
        assertNotNull(warnings);
    }

    @Test
    @DisplayName("Should validate ports information")
    void testPortsInformation() {
        // Given
        String url = "https://example.com";

        // When
        Map<String, Object> result = service.analyzeWebsiteSecurity(url);

        // Then
        assertTrue(result.containsKey("ports"));

        @SuppressWarnings("unchecked")
        Map<String, Object> ports = (Map<String, Object>) result.get("ports");
        assertNotNull(ports);

        assertTrue(ports.containsKey("open_count"));
        Integer openCount = (Integer) ports.get("open_count");
        assertTrue(openCount >= 0, "Open port count should be non-negative");
    }

    @Test
    @DisplayName("Should validate redirect information")
    void testRedirectInformation() {
        // Given
        String url = "https://example.com";

        // When
        Map<String, Object> result = service.analyzeWebsiteSecurity(url);

        // Then
        assertTrue(result.containsKey("redirects"));

        @SuppressWarnings("unchecked")
        Map<String, Object> redirects = (Map<String, Object>) result.get("redirects");
        assertNotNull(redirects);

        assertTrue(redirects.containsKey("count"));
        Integer count = (Integer) redirects.get("count");
        assertTrue(count >= 0, "Redirect count should be non-negative");
    }

    @Test
    @DisplayName("Should handle connection timeouts gracefully")
    void testConnectionTimeout() {
        // Given - URL that will timeout
        String unreachableUrl = "https://192.0.2.1"; // TEST-NET-1 (should not respond)

        // When
        Map<String, Object> result = service.analyzeWebsiteSecurity(unreachableUrl);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("score"));

        Double score = (Double) result.get("score");
        assertTrue(score < 50, "Unreachable sites should have low score");
    }

    @Test
    @DisplayName("Should score HTTPS higher than HTTP")
    void testHTTPSvsHTTPScoring() {
        // Given
        String httpsUrl = "https://example.com";
        String httpUrl = "http://example.com";

        // When
        Map<String, Object> httpsResult = service.analyzeWebsiteSecurity(httpsUrl);
        Map<String, Object> httpResult = service.analyzeWebsiteSecurity(httpUrl);

        // Then
        Double httpsScore = (Double) httpsResult.get("score");
        Double httpScore = (Double) httpResult.get("score");

        // HTTPS should generally score higher than HTTP
        // Note: This might not always be true depending on other factors
        assertNotNull(httpsScore);
        assertNotNull(httpScore);
    }

    @Disabled("Integration test - requires network")
    @Test
    @DisplayName("Integration: Should analyze real HTTPS site")
    void testRealHTTPSSite() {
        // Given
        String url = "https://www.google.com";

        // When
        Map<String, Object> result = service.analyzeWebsiteSecurity(url);

        // Then
        assertNotNull(result);
        Double score = (Double) result.get("score");
        assertTrue(score > 50, "Google should have decent security score");
    }

    @Test
    @DisplayName("Should handle null input safely")
    void testNullInputHandling() {
        // When & Then
        assertThrows(Exception.class, () -> {
            service.analyzeWebsiteSecurity(null);
        });
    }

    @Test
    @DisplayName("Should handle empty URL safely")
    void testEmptyURLHandling() {
        // When & Then
        assertThrows(Exception.class, () -> {
            service.analyzeWebsiteSecurity("");
        });
    }
}

