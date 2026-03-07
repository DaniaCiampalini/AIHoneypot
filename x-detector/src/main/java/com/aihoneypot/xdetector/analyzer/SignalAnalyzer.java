package com.aihoneypot.xdetector.analyzer;

import com.aihoneypot.xdetector.model.BotScore;
import com.aihoneypot.xdetector.model.XAccount;

import java.util.Map;

/**
 * Interface for signal analyzers that compute bot scores.
 */
public interface SignalAnalyzer {

    /**
     * Analyze the account and return a partial bot score (0.0 to 1.0).
     *
     * @param account The X account to analyze
     * @return Score between 0.0 (human) and 1.0 (bot)
     */
    double analyze(XAccount account);

    /**
     * Get the weight of this analyzer in the final score (0.0 to 1.0).
     *
     * @return Weight value
     */
    double getWeight();

    /**
     * Get the name of this analyzer.
     *
     * @return Analyzer name
     */
    String getName();

    /**
     * Get detailed signals and their values.
     *
     * @param account The X account
     * @return Map of signal names to values
     */
    Map<String, Object> getSignalDetails(XAccount account);
}

