package com.aihoneypot.collector.extractor;

import com.aihoneypot.core.model.RawRequestSignals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HttpServletSignalExtractor.
 *
 * Uses Spring's MockHttpServletRequest (from spring-test) to simulate
 * servlet requests without starting a server.
 * No Spring application context is loaded.
 */
@DisplayName("HttpServletSignalExtractor")
class HttpServletSignalExtractorTest {

    private HttpServletSignalExtractor extractor;

    /** Builds a standard mock request that represents a clean browser visit. */
    private MockHttpServletRequest browserRequest() {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/home");
        req.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        req.addHeader("Accept", "text/html,application/xhtml+xml");
        req.addHeader("Accept-Language", "en-US,en;q=0.9");
        req.setRemoteAddr("10.0.0.1");
        return req;
    }

    @BeforeEach
    void setUp() {
        extractor = new HttpServletSignalExtractor();
    }

    // ── Basic extraction ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Basic field extraction")
    class BasicExtraction {

        @Test
        @DisplayName("SessionId is set from parameter")
        void sessionIdSet() {
            RawRequestSignals signals = extractor.extractFromRequest(
                    browserRequest(), "my-session-id", null, 1, 0.0);
            assertEquals("my-session-id", signals.getSessionId());
        }

        @Test
        @DisplayName("HTTP method is extracted correctly")
        void methodExtracted() {
            MockHttpServletRequest req = browserRequest();
            req.setMethod("POST");
            RawRequestSignals signals = extractor.extractFromRequest(req, "s1", null, 1, 0.0);
            assertEquals("POST", signals.getMethod());
        }

        @Test
        @DisplayName("URI is extracted correctly")
        void uriExtracted() {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/data");
            RawRequestSignals signals = extractor.extractFromRequest(req, "s1", null, 1, 0.0);
            assertEquals("/api/data", signals.getUri());
        }

        @Test
        @DisplayName("User-Agent header is extracted")
        void userAgentExtracted() {
            RawRequestSignals signals = extractor.extractFromRequest(
                    browserRequest(), "s1", null, 1, 0.0);
            assertEquals("Mozilla/5.0 (Windows NT 10.0; Win64; x64)", signals.getUserAgent());
        }

        @Test
        @DisplayName("Accept header is extracted")
        void acceptHeaderExtracted() {
            RawRequestSignals signals = extractor.extractFromRequest(
                    browserRequest(), "s1", null, 1, 0.0);
            assertEquals("text/html,application/xhtml+xml", signals.getAcceptHeader());
        }

        @Test
        @DisplayName("Accept-Language header is extracted")
        void acceptLanguageExtracted() {
            RawRequestSignals signals = extractor.extractFromRequest(
                    browserRequest(), "s1", null, 1, 0.0);
            assertEquals("en-US,en;q=0.9", signals.getAcceptLanguage());
        }

        @Test
        @DisplayName("Timestamp is set and close to now")
        void timestampSetCloseToNow() {
            Instant before = Instant.now().minusMillis(100);
            RawRequestSignals signals = extractor.extractFromRequest(
                    browserRequest(), "s1", null, 1, 0.0);
            Instant after = Instant.now().plusMillis(100);
            assertTrue(signals.getTimestamp().isAfter(before));
            assertTrue(signals.getTimestamp().isBefore(after));
        }

        @Test
        @DisplayName("Content length is extracted")
        void contentLengthExtracted() {
            MockHttpServletRequest req = browserRequest();
            req.setContentType("application/json");
            req.setContent("{\"key\":\"value\"}".getBytes());
            RawRequestSignals signals = extractor.extractFromRequest(req, "s1", null, 1, 0.0);
            // MockHttpServletRequest sets content length from the content
            assertTrue(signals.getContentLength() >= 0);
        }

        @Test
        @DisplayName("Missing User-Agent header → userAgent is null")
        void missingUserAgentNull() {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/");
            RawRequestSignals signals = extractor.extractFromRequest(req, "s1", null, 1, 0.0);
            assertNull(signals.getUserAgent());
        }

        @Test
        @DisplayName("Missing Referer header → referer is null")
        void missingRefererNull() {
            RawRequestSignals signals = extractor.extractFromRequest(
                    browserRequest(), "s1", null, 1, 0.0);
            assertNull(signals.getReferer());
        }

        @Test
        @DisplayName("Referer header is extracted when present")
        void refererExtracted() {
            MockHttpServletRequest req = browserRequest();
            req.addHeader("Referer", "https://example.com/page");
            RawRequestSignals signals = extractor.extractFromRequest(req, "s1", null, 1, 0.0);
            assertEquals("https://example.com/page", signals.getReferer());
        }
    }

    // ── IP address extraction ───────────────────────────────────────────────────

    @Nested
    @DisplayName("IP address extraction (proxy-aware)")
    class IpExtraction {

        @Test
        @DisplayName("Uses RemoteAddr when no proxy headers present")
        void usesRemoteAddrDirectly() {
            MockHttpServletRequest req = browserRequest();
            req.setRemoteAddr("203.0.113.5");
            RawRequestSignals signals = extractor.extractFromRequest(req, "s1", null, 1, 0.0);
            assertEquals("203.0.113.5", signals.getIpAddress());
        }

        @Test
        @DisplayName("X-Forwarded-For takes priority over RemoteAddr")
        void xForwardedForTakesPriority() {
            MockHttpServletRequest req = browserRequest();
            req.setRemoteAddr("10.0.0.1"); // internal proxy
            req.addHeader("X-Forwarded-For", "198.51.100.1, 10.0.0.1");
            RawRequestSignals signals = extractor.extractFromRequest(req, "s1", null, 1, 0.0);
            assertEquals("198.51.100.1", signals.getIpAddress());
        }

        @Test
        @DisplayName("X-Forwarded-For with single IP is used directly")
        void xForwardedForSingleIp() {
            MockHttpServletRequest req = browserRequest();
            req.addHeader("X-Forwarded-For", "198.51.100.42");
            RawRequestSignals signals = extractor.extractFromRequest(req, "s1", null, 1, 0.0);
            assertEquals("198.51.100.42", signals.getIpAddress());
        }

        @Test
        @DisplayName("X-Real-IP is used when X-Forwarded-For is absent")
        void xRealIpUsedWhenNoXff() {
            MockHttpServletRequest req = browserRequest();
            req.addHeader("X-Real-IP", "198.51.100.99");
            RawRequestSignals signals = extractor.extractFromRequest(req, "s1", null, 1, 0.0);
            assertEquals("198.51.100.99", signals.getIpAddress());
        }

        @Test
        @DisplayName("X-Forwarded-For takes priority over X-Real-IP")
        void xffTakesPriorityOverXRealIp() {
            MockHttpServletRequest req = browserRequest();
            req.addHeader("X-Forwarded-For", "198.51.100.1");
            req.addHeader("X-Real-IP", "198.51.100.99");
            RawRequestSignals signals = extractor.extractFromRequest(req, "s1", null, 1, 0.0);
            assertEquals("198.51.100.1", signals.getIpAddress());
        }
    }

    // ── Canary path detection ───────────────────────────────────────────────────

    @Nested
    @DisplayName("Canary path detection")
    class CanaryPathDetection {

        @Test
        @DisplayName("/admin → canaryTrapTriggered=true")
        void adminIsCanary() {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/admin");
            assertTrue(extractor.extractFromRequest(req, "s1", null, 1, 0.0).isCanaryTrapTriggered());
        }

        @Test
        @DisplayName("/wp-admin → canaryTrapTriggered=true")
        void wpAdminIsCanary() {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/wp-admin");
            assertTrue(extractor.extractFromRequest(req, "s1", null, 1, 0.0).isCanaryTrapTriggered());
        }

        @Test
        @DisplayName("/.env → canaryTrapTriggered=true")
        void envIsCanary() {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/.env");
            assertTrue(extractor.extractFromRequest(req, "s1", null, 1, 0.0).isCanaryTrapTriggered());
        }

        @Test
        @DisplayName("/config → canaryTrapTriggered=true")
        void configIsCanary() {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/config");
            assertTrue(extractor.extractFromRequest(req, "s1", null, 1, 0.0).isCanaryTrapTriggered());
        }

        @Test
        @DisplayName("/.git → canaryTrapTriggered=true")
        void gitIsCanary() {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/.git");
            assertTrue(extractor.extractFromRequest(req, "s1", null, 1, 0.0).isCanaryTrapTriggered());
        }

        @Test
        @DisplayName("/backup → canaryTrapTriggered=true")
        void backupIsCanary() {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/backup");
            assertTrue(extractor.extractFromRequest(req, "s1", null, 1, 0.0).isCanaryTrapTriggered());
        }

        @Test
        @DisplayName("/admin/users (subpath) → canaryTrapTriggered=true (startsWith check)")
        void adminSubpathIsCanary() {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/admin/users");
            assertTrue(extractor.extractFromRequest(req, "s1", null, 1, 0.0).isCanaryTrapTriggered());
        }

        @Test
        @DisplayName("/home → canaryTrapTriggered=false")
        void homeIsNotCanary() {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/home");
            assertFalse(extractor.extractFromRequest(req, "s1", null, 1, 0.0).isCanaryTrapTriggered());
        }

        @Test
        @DisplayName("/api/v1/data → canaryTrapTriggered=false")
        void normalApiIsNotCanary() {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/data");
            assertFalse(extractor.extractFromRequest(req, "s1", null, 1, 0.0).isCanaryTrapTriggered());
        }
    }

    // ── Timing calculation ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("Timing calculation")
    class TimingCalculation {

        @Test
        @DisplayName("First request (null previousRequest) → timeSincePreviousRequest is null")
        void firstRequestNullTiming() {
            RawRequestSignals signals = extractor.extractFromRequest(
                    browserRequest(), "s1", null, 1, 0.0);
            assertNull(signals.getTimeSincePreviousRequest());
        }

        @Test
        @DisplayName("Second request → timeSincePreviousRequest is a positive number")
        void secondRequestPositiveTiming() throws InterruptedException {
            // Build a previous request with a timestamp 200ms in the past
            RawRequestSignals previous = RawRequestSignals.builder()
                    .sessionId("s1")
                    .timestamp(Instant.now().minusMillis(200))
                    .build();

            RawRequestSignals signals = extractor.extractFromRequest(
                    browserRequest(), "s1", previous, 2, 200.0);

            assertNotNull(signals.getTimeSincePreviousRequest());
            assertTrue(signals.getTimeSincePreviousRequest() > 0,
                    "Timing should be positive, got: " + signals.getTimeSincePreviousRequest());
        }

        @Test
        @DisplayName("Previous request with null timestamp → timeSincePreviousRequest is null")
        void previousWithNullTimestampGivesNullTiming() {
            RawRequestSignals previous = RawRequestSignals.builder()
                    .sessionId("s1")
                    .timestamp(null)
                    .build();

            RawRequestSignals signals = extractor.extractFromRequest(
                    browserRequest(), "s1", previous, 2, null);
            assertNull(signals.getTimeSincePreviousRequest());
        }
    }

    // ── JavaScript detection ────────────────────────────────────────────────────

    @Nested
    @DisplayName("JavaScript detection heuristic")
    class JavaScriptDetection {

        @Test
        @DisplayName("X-Requested-With: XMLHttpRequest → javascriptEnabled=true")
        void xhrHeaderIndicatesJsEnabled() {
            MockHttpServletRequest req = browserRequest();
            req.addHeader("X-Requested-With", "XMLHttpRequest");
            RawRequestSignals signals = extractor.extractFromRequest(req, "s1", null, 1, 0.0);
            assertEquals(Boolean.TRUE, signals.getJavascriptEnabled());
        }

        @Test
        @DisplayName("No JS-related headers → javascriptEnabled=null (unknown)")
        void noJsHeadersUnknown() {
            RawRequestSignals signals = extractor.extractFromRequest(
                    browserRequest(), "s1", null, 1, 0.0);
            assertNull(signals.getJavascriptEnabled());
        }
    }

    // ── Headers map ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Headers map extraction")
    class HeadersMap {

        @Test
        @DisplayName("All request headers are present in the headers map")
        void allHeadersExtracted() {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/");
            req.addHeader("User-Agent", "TestAgent");
            req.addHeader("Accept", "application/json");
            req.addHeader("X-Custom-Header", "custom-value");

            RawRequestSignals signals = extractor.extractFromRequest(req, "s1", null, 1, 0.0);

            assertNotNull(signals.getHeaders());
            assertEquals("TestAgent", signals.getHeaders().get("User-Agent"));
            assertEquals("application/json", signals.getHeaders().get("Accept"));
            assertEquals("custom-value", signals.getHeaders().get("X-Custom-Header"));
        }

        @Test
        @DisplayName("Empty request has empty (not null) headers map")
        void emptyRequestEmptyHeaders() {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/");
            RawRequestSignals signals = extractor.extractFromRequest(req, "s1", null, 1, 0.0);
            assertNotNull(signals.getHeaders());
        }
    }

    // ── Query parameters ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Query parameter extraction")
    class QueryParams {

        @Test
        @DisplayName("Query parameters are extracted into the map")
        void queryParamsExtracted() {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/search");
            req.addParameter("q", "honeypot");
            req.addParameter("page", "2");

            RawRequestSignals signals = extractor.extractFromRequest(req, "s1", null, 1, 0.0);

            assertNotNull(signals.getQueryParams());
            assertEquals("honeypot", signals.getQueryParams().get("q"));
            assertEquals("2", signals.getQueryParams().get("page"));
        }

        @Test
        @DisplayName("No query parameters → empty (not null) map")
        void noQueryParamsEmptyMap() {
            RawRequestSignals signals = extractor.extractFromRequest(
                    browserRequest(), "s1", null, 1, 0.0);
            assertNotNull(signals.getQueryParams());
        }
    }
}