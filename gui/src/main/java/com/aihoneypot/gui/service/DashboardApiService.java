package com.aihoneypot.gui.service;

import com.aihoneypot.dashboard.dto.ThreatSessionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * REST client service to communicate with AIHoneypot backend API.
 */
public class DashboardApiService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public DashboardApiService(String baseUrl) {
        String nonNullBaseUrl = Objects.requireNonNull(baseUrl, "baseUrl");
        this.baseUrl = nonNullBaseUrl;
        this.webClient = WebClient.builder()
            .baseUrl(nonNullBaseUrl)
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Get overall statistics.
     */
    public Map<String, Object> getStatistics() {
        try {
            String json = webClient.get()
                    .uri("/api/dashboard/stats")
                    .retrieve()
                .bodyToMono(String.class)
                    .block();
            return parseMap(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            System.err.println("Error fetching statistics: " + e.getMessage());
            return Map.of();
        }
    }

    /**
     * Get recent threats.
     */
    public List<ThreatSessionDTO> getRecentThreats(int limit) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/dashboard/threats/recent")
                            .queryParam("limit", limit)
                            .build())
                    .retrieve()
                    .bodyToFlux(ThreatSessionDTO.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            System.err.println("Error fetching recent threats: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Get threats in the last N hours.
     */
    public List<ThreatSessionDTO> getThreatsLastHours(int hours) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/dashboard/threats/last-hours")
                            .queryParam("hours", hours)
                            .build())
                    .retrieve()
                    .bodyToFlux(ThreatSessionDTO.class)
                    .collectList()
                    .block();
        } catch (Exception e) {
            System.err.println("Error fetching threats: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Get threat count by client type.
     */
    public Map<String, Long> getThreatsByClientType() {
        try {
            String json = webClient.get()
                    .uri("/api/dashboard/stats/by-client-type")
                    .retrieve()
                .bodyToMono(String.class)
                    .block();
            return parseMap(json, new TypeReference<Map<String, Long>>() {});
        } catch (Exception e) {
            System.err.println("Error fetching client type stats: " + e.getMessage());
            return Map.of();
        }
    }

    /**
     * Get threat count by severity.
     */
    public Map<String, Long> getThreatsBySeverity() {
        try {
            String json = webClient.get()
                    .uri("/api/dashboard/stats/by-severity")
                    .retrieve()
                .bodyToMono(String.class)
                    .block();
            return parseMap(json, new TypeReference<Map<String, Long>>() {});
        } catch (Exception e) {
            System.err.println("Error fetching severity stats: " + e.getMessage());
            return Map.of();
        }
    }

    /**
     * Get top attacking IPs.
     */
    public List<Map<String, Object>> getTopAttackingIPs(int limit) {
        try {
            String json = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/dashboard/stats/top-ips")
                            .queryParam("limit", limit)
                            .build())
                    .retrieve()
                .bodyToMono(String.class)
                    .block();

            return parseList(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            System.err.println("Error fetching top IPs: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Check API health.
     */
    public boolean isHealthy() {
        try {
            String json = webClient.get()
                    .uri("/api/dashboard/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            Map<String, String> response = parseMap(json, new TypeReference<Map<String, String>>() {});
            return response != null && "UP".equals(response.get("status"));
        } catch (Exception e) {
            return false;
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    private <T> T parseMap(String json, TypeReference<T> typeReference) throws Exception {
        if (json == null || json.isBlank()) {
            return null;
        }
        return objectMapper.readValue(json, typeReference);
    }

    private <T> T parseList(String json, TypeReference<T> typeReference) throws Exception {
        if (json == null || json.isBlank()) {
            return null;
        }
        return objectMapper.readValue(json, typeReference);
    }
}

