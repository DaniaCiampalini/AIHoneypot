package com.aihoneypot.gui.service;

import com.aihoneypot.core.model.ClientType;
import com.aihoneypot.core.model.Severity;
import com.aihoneypot.dashboard.dto.ThreatSessionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.Map;

/**
 * REST client service to communicate with AIHoneypot backend API.
 */
public class DashboardApiService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public DashboardApiService(String baseUrl) {
        this.baseUrl = baseUrl;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Get overall statistics.
     */
    public Map<String, Object> getStatistics() {
        try {
            return webClient.get()
                    .uri("/api/dashboard/stats")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>(){})
                    .block();
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
            return webClient.get()
                    .uri("/api/dashboard/stats/by-client-type")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Long>>(){})
                    .block();
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
            return webClient.get()
                    .uri("/api/dashboard/stats/by-severity")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Long>>(){})
                    .block();
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
            List<Map<String, Object>> rawList = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/dashboard/stats/top-ips")
                            .queryParam("limit", limit)
                            .build())
                    .retrieve()
                    .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>(){})
                    .collectList()
                    .block();

            return rawList != null ? rawList : List.of();
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
            Map<String, String> response = webClient.get()
                    .uri("/api/dashboard/health")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, String>>(){})
                    .block();
            return response != null && "UP".equals(response.get("status"));
        } catch (Exception e) {
            return false;
        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
