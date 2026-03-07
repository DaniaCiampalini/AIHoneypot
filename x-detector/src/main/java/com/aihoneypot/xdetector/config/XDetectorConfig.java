package com.aihoneypot.xdetector.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for X Detector module.
 */
@Configuration
@ComponentScan(basePackages = "com.aihoneypot.xdetector")
public class XDetectorConfig {
    // Configuration is handled via component scanning
}

