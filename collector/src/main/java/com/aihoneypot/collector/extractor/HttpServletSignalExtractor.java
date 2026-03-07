package com.aihoneypot.collector.extractor;

import com.aihoneypot.core.interfaces.BehaviorSignalExtractor;
import com.aihoneypot.core.model.RawRequestSignals;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Extracts raw behavioral signals from HttpServletRequest objects.
 * This is the bridge between the servlet filter and the core domain model.
 */
@Component
public class HttpServletSignalExtractor {

    private static final Set<String> CANARY_PATHS = Set.of(
        "/admin", "/wp-admin", "/.env", "/config",
        "/api/internal", "/.git", "/backup"
    );

    /**
     * Extract raw signals from an HTTP servlet request.
     *
     * @param request The servlet request
     * @param sessionId The session identifier
     * @param previousRequest Previous request for timing calculation
     * @return Raw request signals
     */
    public RawRequestSignals extractFromRequest(HttpServletRequest request,
                                                 String sessionId,
                                                 RawRequestSignals previousRequest) {
        Instant now = Instant.now();

        return RawRequestSignals.builder()
            .sessionId(sessionId)
            .timestamp(now)
            .ipAddress(extractIpAddress(request))
            .method(request.getMethod())
            .uri(request.getRequestURI())
            .userAgent(request.getHeader("User-Agent"))
            .referer(request.getHeader("Referer"))
            .acceptHeader(request.getHeader("Accept"))
            .acceptLanguage(request.getHeader("Accept-Language"))
            .headers(extractHeaders(request))
            .cookies(extractCookies(request))
            .queryParams(extractQueryParams(request))
            .timeSincePreviousRequest(calculateTimeSincePrevious(previousRequest, now))
            .canaryTrapTriggered(isCanaryPath(request.getRequestURI()))
            .javascriptEnabled(checkJavaScriptEnabled(request))
            .contentLength(request.getContentLengthLong())
            .build();
    }

    /**
     * Extract real IP address, considering proxies.
     */
    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }

    /**
     * Extract all HTTP headers.
     */
    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }
        return headers;
    }

    /**
     * Extract cookies.
     */
    private Map<String, String> extractCookies(HttpServletRequest request) {
        Map<String, String> cookies = new HashMap<>();
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                cookies.put(cookie.getName(), cookie.getValue());
            }
        }
        return cookies;
    }

    /**
     * Extract query parameters.
     */
    private Map<String, String> extractQueryParams(HttpServletRequest request) {
        return request.getParameterMap().entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().length > 0 ? e.getValue()[0] : ""
            ));
    }

    /**
     * Calculate time since previous request.
     */
    private Long calculateTimeSincePrevious(RawRequestSignals previousRequest, Instant now) {
        if (previousRequest == null || previousRequest.getTimestamp() == null) {
            return null;
        }
        return now.toEpochMilli() - previousRequest.getTimestamp().toEpochMilli();
    }

    /**
     * Check if the URI is a canary trap path.
     */
    private boolean isCanaryPath(String uri) {
        return CANARY_PATHS.stream().anyMatch(uri::startsWith);
    }

    /**
     * Heuristic to detect if JavaScript is enabled.
     * More sophisticated checks could be done client-side with fingerprinting.
     */
    private Boolean checkJavaScriptEnabled(HttpServletRequest request) {
        // Simple heuristic: check for common JS-added headers or cookies
        String xRequestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            return true;
        }
        // Could check for presence of specific cookies set by JS
        return null; // Unknown
    }
}

