package com.aihoneypot.honeypot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application entry point for AIHoneypot.
 *
 * This honeypot server detects and classifies AI agents, bots,
 * and automated HTTP clients using behavioral fingerprinting.
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {
    "com.aihoneypot.core",
    "com.aihoneypot.collector",
    "com.aihoneypot.analyzer",
    "com.aihoneypot.dashboard",
    "com.aihoneypot.xdetector",
    "com.aihoneypot.honeypot"
})
@EntityScan(basePackages = "com.aihoneypot.analyzer.entity")
@EnableJpaRepositories(basePackages = "com.aihoneypot.analyzer.repository")
public class AIHoneypotApplication {

    public static void main(String[] args) {
        SpringApplication.run(AIHoneypotApplication.class, args);
    }
}

