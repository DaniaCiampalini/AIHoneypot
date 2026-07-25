package com.aihoneypot.core.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.Objects;
import java.util.Map;

/**
 * Raw HTTP request signals collected by the filter layer.
 * This is the input to the classification pipeline.
 */
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

    public RawRequestSignals() {
    }

    public RawRequestSignals(String sessionId,
                             Instant timestamp,
                             String ipAddress,
                             String method,
                             String uri,
                             String userAgent,
                             String referer,
                             String acceptHeader,
                             String acceptLanguage,
                             Map<String, String> headers,
                             Map<String, String> cookies,
                             Map<String, String> queryParams,
                             Long timeSincePreviousRequest,
                             boolean canaryTrapTriggered,
                             Boolean javascriptEnabled,
                             long contentLength) {
        this.sessionId = sessionId;
        this.timestamp = timestamp;
        this.ipAddress = ipAddress;
        this.method = method;
        this.uri = uri;
        this.userAgent = userAgent;
        this.referer = referer;
        this.acceptHeader = acceptHeader;
        this.acceptLanguage = acceptLanguage;
        this.headers = headers;
        this.cookies = cookies;
        this.queryParams = queryParams;
        this.timeSincePreviousRequest = timeSincePreviousRequest;
        this.canaryTrapTriggered = canaryTrapTriggered;
        this.javascriptEnabled = javascriptEnabled;
        this.contentLength = contentLength;
    }

    public static RawRequestSignalsBuilder builder() {
        return new RawRequestSignalsBuilder();
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getAcceptHeader() {
        return acceptHeader;
    }

    public void setAcceptHeader(String acceptHeader) {
        this.acceptHeader = acceptHeader;
    }

    public String getAcceptLanguage() {
        return acceptLanguage;
    }

    public void setAcceptLanguage(String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
    }

    public Long getTimeSincePreviousRequest() {
        return timeSincePreviousRequest;
    }

    public void setTimeSincePreviousRequest(Long timeSincePreviousRequest) {
        this.timeSincePreviousRequest = timeSincePreviousRequest;
    }

    public boolean isCanaryTrapTriggered() {
        return canaryTrapTriggered;
    }

    public void setCanaryTrapTriggered(boolean canaryTrapTriggered) {
        this.canaryTrapTriggered = canaryTrapTriggered;
    }

    public Boolean getJavascriptEnabled() {
        return javascriptEnabled;
    }

    public void setJavascriptEnabled(Boolean javascriptEnabled) {
        this.javascriptEnabled = javascriptEnabled;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RawRequestSignals that = (RawRequestSignals) o;
        return canaryTrapTriggered == that.canaryTrapTriggered
                && contentLength == that.contentLength
                && Objects.equals(sessionId, that.sessionId)
                && Objects.equals(timestamp, that.timestamp)
                && Objects.equals(ipAddress, that.ipAddress)
                && Objects.equals(method, that.method)
                && Objects.equals(uri, that.uri)
                && Objects.equals(userAgent, that.userAgent)
                && Objects.equals(referer, that.referer)
                && Objects.equals(acceptHeader, that.acceptHeader)
                && Objects.equals(acceptLanguage, that.acceptLanguage)
                && Objects.equals(headers, that.headers)
                && Objects.equals(cookies, that.cookies)
                && Objects.equals(queryParams, that.queryParams)
                && Objects.equals(timeSincePreviousRequest, that.timeSincePreviousRequest)
                && Objects.equals(javascriptEnabled, that.javascriptEnabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, timestamp, ipAddress, method, uri, userAgent, referer, acceptHeader,
                acceptLanguage, headers, cookies, queryParams, timeSincePreviousRequest, canaryTrapTriggered,
                javascriptEnabled, contentLength);
    }

    @Override
    public String toString() {
        return "RawRequestSignals{" +
                "sessionId='" + sessionId + '\'' +
                ", timestamp=" + timestamp +
                ", ipAddress='" + ipAddress + '\'' +
                ", method='" + method + '\'' +
                ", uri='" + uri + '\'' +
                ", userAgent='" + userAgent + '\'' +
                ", referer='" + referer + '\'' +
                ", acceptHeader='" + acceptHeader + '\'' +
                ", acceptLanguage='" + acceptLanguage + '\'' +
                ", headers=" + headers +
                ", cookies=" + cookies +
                ", queryParams=" + queryParams +
                ", timeSincePreviousRequest=" + timeSincePreviousRequest +
                ", canaryTrapTriggered=" + canaryTrapTriggered +
                ", javascriptEnabled=" + javascriptEnabled +
                ", contentLength=" + contentLength +
                '}';
    }

    public static final class RawRequestSignalsBuilder {
        private String sessionId;
        private Instant timestamp;
        private String ipAddress;
        private String method;
        private String uri;
        private String userAgent;
        private String referer;
        private String acceptHeader;
        private String acceptLanguage;
        private Map<String, String> headers;
        private Map<String, String> cookies;
        private Map<String, String> queryParams;
        private Long timeSincePreviousRequest;
        private boolean canaryTrapTriggered;
        private Boolean javascriptEnabled;
        private long contentLength;

        private RawRequestSignalsBuilder() {
        }

        public RawRequestSignalsBuilder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public RawRequestSignalsBuilder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public RawRequestSignalsBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public RawRequestSignalsBuilder method(String method) {
            this.method = method;
            return this;
        }

        public RawRequestSignalsBuilder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public RawRequestSignalsBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public RawRequestSignalsBuilder referer(String referer) {
            this.referer = referer;
            return this;
        }

        public RawRequestSignalsBuilder acceptHeader(String acceptHeader) {
            this.acceptHeader = acceptHeader;
            return this;
        }

        public RawRequestSignalsBuilder acceptLanguage(String acceptLanguage) {
            this.acceptLanguage = acceptLanguage;
            return this;
        }

        public RawRequestSignalsBuilder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public RawRequestSignalsBuilder cookies(Map<String, String> cookies) {
            this.cookies = cookies;
            return this;
        }

        public RawRequestSignalsBuilder queryParams(Map<String, String> queryParams) {
            this.queryParams = queryParams;
            return this;
        }

        public RawRequestSignalsBuilder timeSincePreviousRequest(Long timeSincePreviousRequest) {
            this.timeSincePreviousRequest = timeSincePreviousRequest;
            return this;
        }

        public RawRequestSignalsBuilder canaryTrapTriggered(boolean canaryTrapTriggered) {
            this.canaryTrapTriggered = canaryTrapTriggered;
            return this;
        }

        public RawRequestSignalsBuilder javascriptEnabled(Boolean javascriptEnabled) {
            this.javascriptEnabled = javascriptEnabled;
            return this;
        }

        public RawRequestSignalsBuilder contentLength(long contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        public RawRequestSignals build() {
            return new RawRequestSignals(sessionId, timestamp, ipAddress, method, uri, userAgent, referer,
                    acceptHeader, acceptLanguage, headers, cookies, queryParams, timeSincePreviousRequest,
                    canaryTrapTriggered, javascriptEnabled, contentLength);
        }
    }
}

