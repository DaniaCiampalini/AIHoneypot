package com.aihoneypot.analyzer;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Test configuration for Spring Boot integration tests.
 * Not a test class, but a configuration class used by integration tests.
 */
@SpringBootApplication(scanBasePackages = {
        "com.aihoneypot.analyzer",
        "com.aihoneypot.core"
})
@EntityScan(basePackages = "com.aihoneypot.analyzer.entity")
@EnableJpaRepositories(basePackages = "com.aihoneypot.analyzer.repository")
public class TestAnalyzerApplication {
}
