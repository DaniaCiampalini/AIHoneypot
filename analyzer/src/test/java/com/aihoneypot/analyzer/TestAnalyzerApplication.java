package com.aihoneypot.analyzer;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.aihoneypot.analyzer",
        "com.aihoneypot.core"
})
@EntityScan(basePackages = "com.aihoneypot.analyzer.entity")
@EnableJpaRepositories(basePackages = "com.aihoneypot.analyzer.repository")
public class TestAnalyzerApplication {
}
