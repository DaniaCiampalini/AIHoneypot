package com.aihoneypot.core.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for RawRequestSignals model.
 * Tests data validation, builder pattern, and business logic.
 */
@DisplayName("RawRequestSignals Tests")
class RawRequestSignalsTest {

    private RawRequestSignals.RawRequestSignalsBuilder builder;

    @BeforeEach
    void setUp() {
        builder = RawRequestSignals.builder()
                .sessionId("test-session-123")
                .ipAddress("192.168.1.100")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .uri("/api/test")
                .method("GET")
                .timestamp(Instant.now());
    }

    @Test
    @DisplayName("Should create valid RawRequestSignals with builder")
    void testBuilderPattern() {
        // Given
        String sessionId = "session-001";
        String ipAddress = "10.0.0.1";

        // When
        RawRequestSignals signals = RawRequestSignals.builder()
                .sessionId(sessionId)
                .ipAddress(ipAddress)
                .userAgent("TestAgent/1.0")
                .uri("/test")
                .method("POST")
                .timestamp(Instant.now())
                .build();

        // Then
        assertNotNull(signals);
        assertEquals(sessionId, signals.getSessionId());
        assertEquals(ipAddress, signals.getIpAddress());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullHandling() {
        // Given & When
        RawRequestSignals signals = RawRequestSignals.builder()
                .sessionId("test")
                .ipAddress(null)
                .userAgent(null)
                .build();

        // Then
        assertNotNull(signals);
        assertNull(signals.getIpAddress());
        assertNull(signals.getUserAgent());
    }

    @Test
    @DisplayName("Should store and retrieve headers correctly")
    void testHeadersHandling() {
        // Given
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer token123");

        // When
        RawRequestSignals signals = builder
                .headers(headers)
                .build();

        // Then
        assertNotNull(signals.getHeaders());
        assertEquals(2, signals.getHeaders().size());
        assertEquals("application/json", signals.getHeaders().get("Content-Type"));
    }

    @Test
    @DisplayName("Should detect suspicious User-Agent patterns")
    void testSuspiciousUserAgent() {
        // Given
        String[] suspiciousAgents = {
            "curl/7.68.0",
            "Python-urllib/3.8",
            "Googlebot/2.1",
            "sqlmap/1.0",
            "Nikto/2.1.6"
        };

        // When & Then
        for (String agent : suspiciousAgents) {
            RawRequestSignals signals = builder.userAgent(agent).build();

            // Business logic: check if user agent contains bot/crawler indicators
            boolean isSuspicious = agent.toLowerCase().matches(".*(bot|crawler|spider|scraper|curl|python|nikto|sqlmap).*");
            assertTrue(isSuspicious, "Should detect suspicious agent: " + agent);
        }
    }

    @Test
    @DisplayName("Should validate IP address format")
    void testIpAddressValidation() {
        // Given
        String[] validIPs = {"192.168.1.1", "10.0.0.1", "172.16.0.1", "8.8.8.8"};
        String[] invalidIPs = {"999.999.999.999", "not-an-ip", "192.168.1"};

        // When & Then - Valid IPs
        for (String ip : validIPs) {
            boolean isValid = ip.matches("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
            assertTrue(isValid, "Should validate IP: " + ip);
        }

        // Invalid IPs
        for (String ip : invalidIPs) {
            boolean isValid = ip.matches("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
            assertFalse(isValid, "Should reject invalid IP: " + ip);
        }
    }

    @Test
    @DisplayName("Should detect canary trap paths")
    void testCanaryTrapDetection() {
        // Given
        String[] canaryPaths = {
            "/admin",
            "/wp-admin",
            "/.env",
            "/backup",
            "/.git",
            "/config.php",
            "/phpmyadmin"
        };

        // When & Then
        for (String path : canaryPaths) {
            RawRequestSignals signals = builder.uri(path).build();

            boolean isCanary = path.matches(".*(admin|wp-admin|\\.env|backup|\\.git|config|phpmyadmin).*");
            assertTrue(isCanary, "Should detect canary trap: " + path);
        }
    }

    @Test
    @DisplayName("Should handle timestamp correctly")
    void testTimestampHandling() {
        // Given
        Instant now = Instant.now();
        Instant past = now.minusSeconds(3600); // 1 hour ago

        // When
        RawRequestSignals recentSignal = builder.timestamp(now).build();
        RawRequestSignals oldSignal = builder.timestamp(past).build();

        // Then
        assertNotNull(recentSignal.getTimestamp());
        assertNotNull(oldSignal.getTimestamp());
        assertTrue(recentSignal.getTimestamp().isAfter(oldSignal.getTimestamp()));
    }

    @Test
    @DisplayName("Should detect HTTP method anomalies")
    void testHttpMethodValidation() {
        // Given
        String[] validMethods = {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"};
        String[] unusualMethods = {"TRACE", "CONNECT", "PROPFIND", "CUSTOM"};

        // When & Then - Valid methods
        for (String httpMethod : validMethods) {
            RawRequestSignals signals = builder.method(httpMethod).build();
            assertNotNull(signals.getMethod());
        }

        // Unusual methods (might indicate probing)
        for (String httpMethod : unusualMethods) {
            RawRequestSignals signals = builder.method(httpMethod).build();
            boolean isUnusual = !httpMethod.matches("GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS");
            assertTrue(isUnusual, "Should flag unusual method: " + httpMethod);
        }
    }

    @Test
    @DisplayName("Should handle query parameters correctly")
    void testQueryParameters() {
        // Given
        Map<String, String> params = new HashMap<>();
        params.put("id", "123");
        params.put("token", "abc");

        // When
        RawRequestSignals signals = builder.queryParams(params).build();

        // Then
        assertNotNull(signals.getQueryParams());
        assertEquals("123", signals.getQueryParams().get("id"));
    }

    @Test
    @DisplayName("Should detect SQL injection attempts in parameters")
    void testSQLInjectionDetection() {
        // Given
        String[] sqlInjectionPatterns = {
            "1' OR '1'='1",
            "admin'--",
            "' UNION SELECT",
            "'; DROP TABLE users--"
        };

        // When & Then
        for (String pattern : sqlInjectionPatterns) {
            Map<String, String> params = new HashMap<>();
            params.put("input", pattern);

            RawRequestSignals signals = builder.queryParams(params).build();

            boolean isSQLi = pattern.matches(".*('|--|UNION|DROP|SELECT|INSERT|DELETE|UPDATE|;).*");
            assertTrue(isSQLi, "Should detect SQL injection: " + pattern);
        }
    }

    @Test
    @DisplayName("Should compare signals for equality")
    void testEqualsAndHashCode() {
        // Given
        Instant timestamp = Instant.now();
        RawRequestSignals signals1 = RawRequestSignals.builder()
                .sessionId("session-1")
                .ipAddress("192.168.1.1")
                .timestamp(timestamp)
                .build();

        RawRequestSignals signals2 = RawRequestSignals.builder()
                .sessionId("session-1")
                .ipAddress("192.168.1.1")
                .timestamp(timestamp)
                .build();

        RawRequestSignals signals3 = RawRequestSignals.builder()
                .sessionId("session-2")
                .ipAddress("192.168.1.2")
                .timestamp(timestamp)
                .build();

        // Then
        assertEquals(signals1, signals2);
        assertNotEquals(signals1, signals3);
        assertEquals(signals1.hashCode(), signals2.hashCode());
    }
}

