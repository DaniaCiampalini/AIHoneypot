package com.aihoneypot.core.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Raw HTTP request signals collected by the filter layer.
 * This is the input to the classification pipeline.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawRequestSignals {

    /**
     * Unique session identifier
     */
    private String sessionId;

    /**
     * Timestamp of the request
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;

    /**
     * Source IP address
     */
    private String ipAddress;

    /**
     * HTTP method (GET, POST, etc.)
     */
    private String method;

    /**
     * Requested URI
     */
    private String uri;

    /**
     * User-Agent header
     */
    private String userAgent;

    /**
     * Referer header (nullable)
     */
    private String referer;

    /**
     * Accept header (nullable)
     */
    private String acceptHeader;

    /**
     * Accept-Language header (nullable)
     */
    private String acceptLanguage;

    /**
     * All HTTP headers as key-value pairs
     */
    private Map<String, String> headers;

    /**
     * Request cookies
     */
    private Map<String, String> cookies;

    /**
     * Query parameters
     */
    private Map<String, String> queryParams;

    /**
     * Time elapsed since previous request in milliseconds (null for first request)
     */
    private Long timeSincePreviousRequest;

    /**
     * Whether this request hit a canary trap endpoint
     */
    private boolean canaryTrapTriggered;

    /**
     * Whether JavaScript appears to be enabled (based on client capabilities)
     */
    private Boolean javascriptEnabled;

    /**
     * Request body size in bytes
     */
    private long contentLength;
}

