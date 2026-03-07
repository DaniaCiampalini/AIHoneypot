package com.aihoneypot.collector.filter;

import com.aihoneypot.collector.extractor.HttpServletSignalExtractor;
import com.aihoneypot.collector.service.SessionStore;
import com.aihoneypot.core.model.RawRequestSignals;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Servlet filter that intercepts all HTTP requests to collect behavioral signals.
 * This is the entry point for the signal collection pipeline.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SignalCollectorFilter implements Filter {

    private final HttpServletSignalExtractor signalExtractor;
    private final SessionStore sessionStore;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpRequest) {
            try {
                collectSignals(httpRequest);
            } catch (Exception e) {
                log.error("Error collecting signals: {}", e.getMessage(), e);
                // Continue processing even if signal collection fails
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * Collect signals from the HTTP request.
     */
    private void collectSignals(HttpServletRequest request) {
        String sessionId = getOrCreateSessionId(request);

        // Get previous request for timing analysis
        RawRequestSignals previousRequest = sessionStore.getPreviousRequest(sessionId);

        // Extract signals from current request
        RawRequestSignals signals = signalExtractor.extractFromRequest(
            request,
            sessionId,
            previousRequest
        );

        // Store in session store
        sessionStore.addRequest(sessionId, signals);

        log.debug("Collected signals for session {} - Request #{}: {} {}",
            sessionId,
            sessionStore.getRequestCount(sessionId),
            signals.getMethod(),
            signals.getUri()
        );

        // Log canary trap hits
        if (signals.isCanaryTrapTriggered()) {
            log.warn("CANARY TRAP triggered by session {} - IP: {} - URI: {}",
                sessionId,
                signals.getIpAddress(),
                signals.getUri()
            );
        }
    }

    /**
     * Get or create a session ID.
     */
    private String getOrCreateSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        return session.getId();
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("SignalCollectorFilter initialized");
    }

    @Override
    public void destroy() {
        log.info("SignalCollectorFilter destroyed");
    }
}

