package com.aihoneypot.gui.service;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Advanced security analysis service with multiple checks.
 */
public class SecurityAnalysisService {

    /**
     * Perform comprehensive website security analysis.
     */
    public Map<String, Object> analyzeWebsiteSecurity(String urlString) {
        Map<String, Object> result = new HashMap<>();
        List<String> vulnerabilities = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> goodPractices = new ArrayList<>();

        double securityScore = 100.0;

        try {
            URL url = new URL(urlString);

            // 1. SSL/TLS Check
            Map<String, Object> sslCheck = checkSSL(url);
            result.put("ssl", sslCheck);
            if (!(Boolean) sslCheck.get("valid")) {
                vulnerabilities.add("❌ SSL/TLS certificate invalid or missing");
                securityScore -= 30;
            } else {
                goodPractices.add("✅ Valid SSL/TLS certificate");
            }

            // 2. HTTP Security Headers
            Map<String, Object> headersCheck = checkSecurityHeaders(url);
            result.put("headers", headersCheck);
            int missingHeaders = (Integer) headersCheck.get("missing_count");
            if (missingHeaders > 3) {
                vulnerabilities.add("❌ Missing critical security headers (" + missingHeaders + ")");
                securityScore -= 20;
            } else if (missingHeaders > 0) {
                warnings.add("⚠️ Some security headers missing (" + missingHeaders + ")");
                securityScore -= 5 * missingHeaders;
            } else {
                goodPractices.add("✅ All security headers present");
            }

            // 3. URL Pattern Analysis
            Map<String, Object> urlCheck = analyzeURLPattern(urlString);
            result.put("url_pattern", urlCheck);
            if ((Boolean) urlCheck.get("suspicious")) {
                vulnerabilities.add("❌ Suspicious URL pattern detected");
                securityScore -= 15;
            }

            // 4. DNS Check
            Map<String, Object> dnsCheck = checkDNS(url.getHost());
            result.put("dns", dnsCheck);
            if (!(Boolean) dnsCheck.get("resolvable")) {
                vulnerabilities.add("❌ DNS resolution issues");
                securityScore -= 10;
            }

            // 5. Port Scan (common vulnerable ports)
            Map<String, Object> portCheck = scanCommonPorts(url.getHost());
            result.put("ports", portCheck);
            int openPorts = (Integer) portCheck.get("open_count");
            if (openPorts > 3) {
                warnings.add("⚠️ Multiple open ports detected (" + openPorts + ")");
                securityScore -= 5;
            }

            // 6. Redirect Chain Analysis
            Map<String, Object> redirectCheck = analyzeRedirects(url);
            result.put("redirects", redirectCheck);
            int redirectCount = (Integer) redirectCheck.get("count");
            if (redirectCount > 3) {
                warnings.add("⚠️ Long redirect chain (" + redirectCount + " hops)");
                securityScore -= 5;
            }

            // 7. Content Analysis
            Map<String, Object> contentCheck = analyzeContent(url);
            result.put("content", contentCheck);
            if ((Boolean) contentCheck.get("has_mixed_content")) {
                vulnerabilities.add("❌ Mixed content (HTTP in HTTPS page)");
                securityScore -= 10;
            }

        } catch (Exception e) {
            vulnerabilities.add("❌ Failed to connect: " + e.getMessage());
            securityScore -= 50;
        }

        // Calculate final score
        securityScore = Math.max(0, Math.min(100, securityScore));

        // Determine security level
        String securityLevel;
        if (securityScore >= 80) {
            securityLevel = "SECURE";
        } else if (securityScore >= 60) {
            securityLevel = "MODERATE";
        } else if (securityScore >= 40) {
            securityLevel = "VULNERABLE";
        } else {
            securityLevel = "CRITICAL";
        }

        result.put("score", securityScore);
        result.put("level", securityLevel);
        result.put("vulnerabilities", vulnerabilities);
        result.put("warnings", warnings);
        result.put("good_practices", goodPractices);

        return result;
    }

    private Map<String, Object> checkSSL(URL url) {
        Map<String, Object> result = new HashMap<>();

        if (!url.getProtocol().equals("https")) {
            result.put("valid", false);
            result.put("reason", "Not HTTPS");
            return result;
        }

        try {
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.connect();

            Certificate[] certs = conn.getServerCertificates();
            if (certs.length > 0 && certs[0] instanceof X509Certificate) {
                X509Certificate cert = (X509Certificate) certs[0];

                // Check expiration
                Date notAfter = cert.getNotAfter();
                Date now = new Date();
                boolean expired = notAfter.before(now);

                result.put("valid", !expired);
                result.put("issuer", cert.getIssuerDN().getName());
                result.put("expires", notAfter.toString());
                result.put("expired", expired);

                // Check if self-signed
                boolean selfSigned = cert.getIssuerDN().equals(cert.getSubjectDN());
                result.put("self_signed", selfSigned);
            }

            conn.disconnect();
        } catch (Exception e) {
            result.put("valid", false);
            result.put("reason", e.getMessage());
        }

        return result;
    }

    private Map<String, Object> checkSecurityHeaders(URL url) {
        Map<String, Object> result = new HashMap<>();
        Map<String, String> headers = new HashMap<>();

        String[] criticalHeaders = {
            "Strict-Transport-Security",
            "X-Frame-Options",
            "X-Content-Type-Options",
            "Content-Security-Policy",
            "X-XSS-Protection",
            "Referrer-Policy",
            "Permissions-Policy"
        };

        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("HEAD");
            conn.connect();

            int missingCount = 0;
            for (String header : criticalHeaders) {
                String value = conn.getHeaderField(header);
                if (value != null) {
                    headers.put(header, value);
                } else {
                    missingCount++;
                }
            }

            result.put("headers", headers);
            result.put("missing_count", missingCount);
            result.put("present_count", criticalHeaders.length - missingCount);

            conn.disconnect();
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("missing_count", criticalHeaders.length);
        }

        return result;
    }

    private Map<String, Object> analyzeURLPattern(String urlString) {
        Map<String, Object> result = new HashMap<>();

        String[] suspiciousPatterns = {
            "admin", "login", "wp-admin", "phpmyadmin",
            ".env", "config", "backup", ".git", ".svn",
            "dump", "sql", "database", "credentials"
        };

        String lowerUrl = urlString.toLowerCase();
        List<String> matches = new ArrayList<>();

        for (String pattern : suspiciousPatterns) {
            if (lowerUrl.contains(pattern)) {
                matches.add(pattern);
            }
        }

        result.put("suspicious", !matches.isEmpty());
        result.put("patterns", matches);
        result.put("is_ip", lowerUrl.matches(".*\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}.*"));

        return result;
    }

    private Map<String, Object> checkDNS(String hostname) {
        Map<String, Object> result = new HashMap<>();

        try {
            InetAddress address = InetAddress.getByName(hostname);
            result.put("resolvable", true);
            result.put("ip", address.getHostAddress());
            result.put("hostname", address.getHostName());
        } catch (Exception e) {
            result.put("resolvable", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    private Map<String, Object> scanCommonPorts(String hostname) {
        Map<String, Object> result = new HashMap<>();

        int[] commonPorts = {21, 22, 23, 25, 80, 443, 3306, 3389, 5432, 8080};
        List<Integer> openPorts = new ArrayList<>();

        for (int port : commonPorts) {
            try {
                java.net.Socket socket = new java.net.Socket();
                socket.connect(new java.net.InetSocketAddress(hostname, port), 1000);
                openPorts.add(port);
                socket.close();
            } catch (Exception e) {
                // Port closed or filtered
            }
        }

        result.put("open_ports", openPorts);
        result.put("open_count", openPorts.size());

        return result;
    }

    private Map<String, Object> analyzeRedirects(URL url) {
        Map<String, Object> result = new HashMap<>();
        List<String> chain = new ArrayList<>();

        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(5000);

            String currentUrl = url.toString();
            int redirectCount = 0;
            int maxRedirects = 10;

            while (redirectCount < maxRedirects) {
                conn = (HttpURLConnection) new URL(currentUrl).openConnection();
                conn.setInstanceFollowRedirects(false);
                conn.connect();

                int responseCode = conn.getResponseCode();
                chain.add(currentUrl + " [" + responseCode + "]");

                if (responseCode >= 300 && responseCode < 400) {
                    String location = conn.getHeaderField("Location");
                    if (location != null) {
                        currentUrl = location;
                        redirectCount++;
                    } else {
                        break;
                    }
                } else {
                    break;
                }

                conn.disconnect();
            }

            result.put("count", redirectCount);
            result.put("chain", chain);

        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("count", 0);
        }

        return result;
    }

    private Map<String, Object> analyzeContent(URL url) {
        Map<String, Object> result = new HashMap<>();

        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.connect();

            String contentType = conn.getContentType();
            int contentLength = conn.getContentLength();

            result.put("content_type", contentType);
            result.put("content_length", contentLength);
            result.put("has_mixed_content", false); // Simplified check

            conn.disconnect();
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }

        return result;
    }
}

