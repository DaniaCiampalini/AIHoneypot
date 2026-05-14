package com.aihoneypot.dashboard;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Test configuration for Spring Boot integration tests.
 * This is not a test class, but a configuration class used by integration tests.
 */
@SpringBootApplication(scanBasePackages = {
        "com.aihoneypot.analyzer",
        "com.aihoneypot.dashboard",
        "com.aihoneypot.core"
})
@EntityScan(basePackages = "com.aihoneypot.analyzer.entity")
@EnableJpaRepositories(basePackages = "com.aihoneypot.analyzer.repository")
public class TestDashboardApplication {
}
