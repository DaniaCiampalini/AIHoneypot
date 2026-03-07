package com.aihoneypot.core.model;

/**
 * Enumeration of detected client types based on behavioral fingerprinting.
 */
public enum ClientType {
    /**
     * Standard web browser used by humans
     */
    HUMAN_BROWSER,

    /**
     * AI agent or LLM-powered bot (e.g., GPT, Claude)
     */
    AI_AGENT,

    /**
     * Automated scraper or crawler
     */
    BOT_SCRAPER,

    /**
     * Legitimate search engine crawler (Google, Bing, etc.)
     */
    SEARCH_ENGINE,

    /**
     * Security scanner or penetration testing tool
     */
    SECURITY_SCANNER,

    /**
     * Unknown or unclassified client
     */
    UNKNOWN
}

