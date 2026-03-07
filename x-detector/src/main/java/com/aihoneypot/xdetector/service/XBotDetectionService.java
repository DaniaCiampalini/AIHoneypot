package com.aihoneypot.xdetector.service;

import com.aihoneypot.xdetector.analyzer.SignalAnalyzer;
import com.aihoneypot.xdetector.model.BotScore;
import com.aihoneypot.xdetector.model.ManualAccountInput;
import com.aihoneypot.xdetector.model.XAccount;
import com.aihoneypot.xdetector.model.XTweet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main service for X bot detection.
 * Orchestrates all signal analyzers and computes final BotScore.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class XBotDetectionService {

    private final List<SignalAnalyzer> analyzers;

    /**
     * Analyze an X account and compute bot score.
     *
     * @param account The account to analyze
     * @return BotScore with final verdict
     */
    public BotScore analyze(XAccount account) {
        log.info("Analyzing X account: @{}", account.getUsername());

        Map<String, Double> categoryScores = new HashMap<>();
        Map<String, Object> signalDetails = new HashMap<>();
        List<String> redFlags = new ArrayList<>();
        List<String> positiveSignals = new ArrayList<>();

        double totalWeightedScore = 0.0;
        double totalWeight = 0.0;

        // Run all analyzers
        for (SignalAnalyzer analyzer : analyzers) {
            double score = analyzer.analyze(account);
            double weight = analyzer.getWeight();
            String name = analyzer.getName();

            categoryScores.put(name, score);
            signalDetails.putAll(analyzer.getSignalDetails(account));

            totalWeightedScore += score * weight;
            totalWeight += weight;

            // Collect red flags
            if (score > 0.6) {
                redFlags.add(name + " detected high bot probability (" +
                    String.format("%.2f", score) + ")");
            }

            log.debug("{}: score={}, weight={}", name, score, weight);
        }

        // Normalize final score
        double finalScore = totalWeight > 0 ? totalWeightedScore / totalWeight : 0.0;
        finalScore = Math.max(0.0, Math.min(1.0, finalScore));

        // Add positive signals
        if (account.isVerified()) {
            positiveSignals.add("Account is verified");
        }
        if (account.getFollowersCount() > 10000) {
            positiveSignals.add("High follower count (" + account.getFollowersCount() + ")");
        }

        // Generate explanation
        String explanation = generateExplanation(finalScore, account, redFlags, positiveSignals);

        // Generate recommendation
        String recommendation = generateRecommendation(finalScore);

        BotScore.BotClassification classification = BotScore.classifyFromScore(finalScore);
        double confidence = calculateConfidence(finalScore, categoryScores);

        BotScore result = BotScore.builder()
            .username(account.getUsername())
            .score(finalScore)
            .classification(classification)
            .confidence(confidence)
            .explanation(explanation)
            .categoryScores(categoryScores)
            .redFlags(redFlags)
            .positiveSignals(positiveSignals)
            .signalDetails(signalDetails)
            .recommendation(recommendation)
            .build();

        log.info("Analysis complete for @{}: score={}, classification={}",
            account.getUsername(), finalScore, classification);

        return result;
    }

    /**
     * Analyze from manual input (no API).
     *
     * @param input Manual account data
     * @return BotScore
     */
    public BotScore analyzeManual(ManualAccountInput input) {
        XAccount account = convertManualInput(input);
        return analyze(account);
    }

    private XAccount convertManualInput(ManualAccountInput input) {
        XAccount.XAccountBuilder builder = XAccount.builder()
            .username(input.getUsername())
            .displayName(input.getDisplayName())
            .bio(input.getBio())
            .verified(input.isVerified())
            .followersCount(input.getFollowersCount())
            .followingCount(input.getFollowingCount())
            .tweetCount(input.getTweetCount())
            .defaultProfileImage(input.isDefaultProfileImage());

        // Parse account creation date
        if (input.getAccountCreatedDate() != null) {
            try {
                builder.createdAt(Instant.parse(input.getAccountCreatedDate()));
            } catch (Exception e) {
                log.warn("Failed to parse account creation date: {}", input.getAccountCreatedDate());
            }
        }

        // Parse recent tweets
        if (input.getRecentTweetTexts() != null && !input.getRecentTweetTexts().isEmpty()) {
            List<XTweet> tweets = new ArrayList<>();
            for (int i = 0; i < input.getRecentTweetTexts().size(); i++) {
                XTweet.XTweetBuilder tweetBuilder = XTweet.builder()
                    .text(input.getRecentTweetTexts().get(i))
                    .tweetId("manual_" + i);

                // Parse timestamp if available
                if (input.getRecentTweetTimestamps() != null &&
                    i < input.getRecentTweetTimestamps().size()) {
                    try {
                        tweetBuilder.createdAt(Instant.parse(input.getRecentTweetTimestamps().get(i)));
                    } catch (Exception e) {
                        log.warn("Failed to parse tweet timestamp");
                    }
                }

                // Add source if available
                if (input.getTweetSources() != null && i < input.getTweetSources().size()) {
                    tweetBuilder.source(input.getTweetSources().get(i));
                }

                tweets.add(tweetBuilder.build());
            }
            builder.recentTweets(tweets);
        }

        return builder.build();
    }

    private String generateExplanation(double score, XAccount account,
                                       List<String> redFlags, List<String> positiveSignals) {
        StringBuilder sb = new StringBuilder();

        if (score > 0.8) {
            sb.append("CONFIRMED BOT: ");
        } else if (score > 0.6) {
            sb.append("LIKELY BOT: ");
        } else if (score > 0.3) {
            sb.append("UNCERTAIN: ");
        } else {
            sb.append("LIKELY HUMAN: ");
        }

        sb.append("Account @").append(account.getUsername())
          .append(" has a bot probability of ").append(String.format("%.1f%%", score * 100))
          .append(". ");

        if (!redFlags.isEmpty()) {
            sb.append("Red flags: ").append(String.join(", ", redFlags)).append(". ");
        }

        if (!positiveSignals.isEmpty()) {
            sb.append("Positive indicators: ").append(String.join(", ", positiveSignals)).append(".");
        }

        return sb.toString();
    }

    private String generateRecommendation(double score) {
        if (score > 0.8) {
            return "BLOCK - High confidence bot detection. Recommend blocking or flagging.";
        } else if (score > 0.6) {
            return "REVIEW - Likely bot. Manual review recommended before action.";
        } else if (score > 0.3) {
            return "MONITOR - Uncertain classification. Continue monitoring activity.";
        } else {
            return "ALLOW - Likely legitimate human account.";
        }
    }

    private double calculateConfidence(double score, Map<String, Double> categoryScores) {
        // Confidence is higher when all analyzers agree
        if (categoryScores.isEmpty()) return 0.5;

        double variance = categoryScores.values().stream()
            .mapToDouble(s -> Math.pow(s - score, 2))
            .average()
            .orElse(0.0);

        // Low variance = high confidence
        double confidence = 1.0 - Math.min(variance, 1.0);
        return Math.max(0.5, confidence); // Min 50% confidence
    }
}

