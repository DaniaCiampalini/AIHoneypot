package com.aihoneypot.dashboard.controller;

import com.aihoneypot.dashboard.TestDashboardApplication;
import com.aihoneypot.dashboard.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestDashboardApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("DashboardController Tests")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @Test
    @DisplayName("Should return dashboard page with status 200")
    void testDashboardPageLoads() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("AIHoneypot Dashboard"));
    }

    @Test
    @DisplayName("Should return health check status")
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/dashboard/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("AIHoneypot Dashboard"));
    }

    @Test
    @DisplayName("Should return statistics")
    void testGetStatistics() throws Exception {
        Map<String, Object> stats = Map.of("total_threats", 42);
        when(dashboardService.getThreatStatistics()).thenReturn(stats);

        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_threats").value(42));
    }

    @Test
    @DisplayName("Should return recent threats with default limit")
    void testGetRecentThreats() throws Exception {
        when(dashboardService.getRecentThreats(50)).thenReturn(List.of());

        mockMvc.perform(get("/api/dashboard/threats/recent"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return threats in last hours")
    void testGetThreatsLastHours() throws Exception {
        when(dashboardService.getRecentThreatsInHours(24)).thenReturn(List.of());

        mockMvc.perform(get("/api/dashboard/threats/last-hours"))
                .andExpect(status().isOk());
    }
}
