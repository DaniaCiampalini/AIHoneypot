package com.aihoneypot.dashboard.controller;

import com.aihoneypot.dashboard.TestDashboardApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestDashboardApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("DashboardController Tests")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should return dashboard page")
    void testDashboardPageLoads() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }
}
