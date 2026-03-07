package com.aihoneypot.xdetector.analyzer;

import com.aihoneypot.xdetector.model.XAccount;
import com.aihoneypot.xdetector.model.XTweet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzes posting frequency and timing patterns.
 * Weight: 25%
 */
@Slf4j
@Component
public class TemporalAnalyzer implements SignalAnalyzer {

    private static final double WEIGHT = 0.25;

    @Override
    public double analyze(XAccount account) {
        if (account.getRecentTweets() == null || account.getRecentTweets().isEmpty()) {
            return 0.0;
        }

        double score = 0.0;
        int signals = 0;

        List<XTweet> tweets = account.getRecentTweets();

        // Signal 1: Posting frequency (tweets per day)
        double tweetsPerDay = calculateTweetsPerDay(tweets);
        if (tweetsPerDay > 50) {
            score += 0.35; // Very high frequency
        } else if (tweetsPerDay > 20) {
            score += 0.20;
        } else if (tweetsPerDay > 10) {
            score += 0.10;
        }
        signals++;

        // Signal 2: Regular interval posting (low variance = bot)
        double intervalVariance = calculateIntervalVariance(tweets);
        if (intervalVariance < 0.2) {
            score += 0.30; // Very regular = automated
        } else if (intervalVariance < 0.5) {
            score += 0.15;
        }
        signals++;

        // Signal 3: Burst posting (many tweets in short time)
        if (hasBurstPosting(tweets)) {
            score += 0.25;
            signals++;
        }

        // Signal 4: Night posting pattern (unusual hours)
        double nightPostingRatio = calculateNightPostingRatio(tweets);
        if (nightPostingRatio > 0.5) {
            score += 0.15; // More than half posted at night
        }
        signals++;

        return signals > 0 ? Math.max(0.0, Math.min(1.0, score)) : 0.0;
    }

    private double calculateTweetsPerDay(List<XTweet> tweets) {
        if (tweets.size() < 2) return 0.0;

        List<XTweet> sorted = tweets.stream()
            .sorted(Comparator.comparing(XTweet::getCreatedAt))
            .collect(Collectors.toList());

        Instant oldest = sorted.get(0).getCreatedAt();
        Instant newest = sorted.get(sorted.size() - 1).getCreatedAt();

        long days = Duration.between(oldest, newest).toDays();
        if (days == 0) days = 1;

        return (double) tweets.size() / days;
    }

    private double calculateIntervalVariance(List<XTweet> tweets) {
        if (tweets.size() < 3) return 1.0;

        List<XTweet> sorted = tweets.stream()
            .sorted(Comparator.comparing(XTweet::getCreatedAt))
            .collect(Collectors.toList());

        List<Long> intervals = new ArrayList<>();
        for (int i = 1; i < sorted.size(); i++) {
            long interval = Duration.between(
                sorted.get(i - 1).getCreatedAt(),
                sorted.get(i).getCreatedAt()
            ).toMinutes();
            intervals.add(interval);
        }

        double[] values = intervals.stream().mapToDouble(Long::doubleValue).toArray();
        StandardDeviation sd = new StandardDeviation();
        double stdDev = sd.evaluate(values);
        double mean = Arrays.stream(values).average().orElse(1.0);

        // Coefficient of variation (CV)
        return mean > 0 ? stdDev / mean : 1.0;
    }

    private boolean hasBurstPosting(List<XTweet> tweets) {
        if (tweets.size() < 5) return false;

        List<XTweet> sorted = tweets.stream()
            .sorted(Comparator.comparing(XTweet::getCreatedAt))
            .collect(Collectors.toList());

        // Check for 5+ tweets within 10 minutes
        for (int i = 0; i <= sorted.size() - 5; i++) {
            Instant start = sorted.get(i).getCreatedAt();
            Instant end = sorted.get(i + 4).getCreatedAt();
            if (Duration.between(start, end).toMinutes() < 10) {
                return true;
            }
        }

        return false;
    }

    private double calculateNightPostingRatio(List<XTweet> tweets) {
        if (tweets.isEmpty()) return 0.0;

        long nightPosts = tweets.stream()
            .filter(tweet -> {
                int hour = tweet.getCreatedAt().atZone(java.time.ZoneId.of("UTC")).getHour();
                return hour >= 1 && hour <= 5; // 1 AM to 5 AM UTC
            })
            .count();

        return (double) nightPosts / tweets.size();
    }

    @Override
    public double getWeight() {
        return WEIGHT;
    }

    @Override
    public String getName() {
        return "TemporalAnalyzer";
    }

    @Override
    public Map<String, Object> getSignalDetails(XAccount account) {
        Map<String, Object> details = new HashMap<>();

        if (account.getRecentTweets() == null || account.getRecentTweets().isEmpty()) {
            return details;
        }

        List<XTweet> tweets = account.getRecentTweets();

        details.put("tweets_per_day", calculateTweetsPerDay(tweets));
        details.put("interval_variance", calculateIntervalVariance(tweets));
        details.put("has_burst_posting", hasBurstPosting(tweets));
        details.put("night_posting_ratio", calculateNightPostingRatio(tweets));
        details.put("total_tweets_analyzed", tweets.size());

        return details;
    }
}

