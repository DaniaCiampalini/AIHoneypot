package com.aihoneypot.xdetector.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Result of bot detection analysis for an X account.
 * Contains the final score, classification, and detailed signal breakdown.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotScore {

    /**
     * Account being analyzed
     */
    private String username;

    /**
     * Final bot probability score (0.0 = human, 1.0 = bot)
     */
    private double score;

    /**
     * Classification label
     */
    private BotClassification classification;

    /**
     * Confidence level of the classification
     */
    private double confidence;

    /**
     * Human-readable explanation
     */
    private String explanation;

    /**
     * Individual signal scores by category
     */
    @Builder.Default
    private Map<String, Double> categoryScores = new HashMap<>();

    /**
     * List of triggered red flags
     */
    @Builder.Default
    private List<String> redFlags = new ArrayList<>();

    /**
     * List of positive signals (reduces bot score)
     */
    @Builder.Default
    private List<String> positiveSignals = new ArrayList<>();

    /**
     * Detailed signal breakdown
     */
    @Builder.Default
    private Map<String, Object> signalDetails = new HashMap<>();

    /**
     * Recommendation for action
     */
    private String recommendation;

    /**
     * Bot classification categories
     */
    public enum BotClassification {
        LIKELY_HUMAN,      // Score < 0.3
        UNCERTAIN,         // Score 0.3 - 0.6
        LIKELY_BOT,        // Score 0.6 - 0.8
        CONFIRMED_BOT      // Score > 0.8
    }

    /**
     * Calculate classification from score
     */
    public static BotClassification classifyFromScore(double score) {
        if (score < 0.3) return BotClassification.LIKELY_HUMAN;
        if (score < 0.6) return BotClassification.UNCERTAIN;
        if (score < 0.8) return BotClassification.LIKELY_BOT;
        return BotClassification.CONFIRMED_BOT;
    }
}

