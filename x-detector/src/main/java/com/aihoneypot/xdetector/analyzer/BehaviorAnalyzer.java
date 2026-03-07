package com.aihoneypot.xdetector.analyzer;

import com.aihoneypot.xdetector.model.XAccount;
import com.aihoneypot.xdetector.model.XTweet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Analyzes general behavioral signals.
 * Weight: 20%
 */
@Slf4j
@Component
public class BehaviorAnalyzer implements SignalAnalyzer {

    private static final double WEIGHT = 0.20;

    private static final Pattern API_CLIENT = Pattern.compile("(?i).*(api|bot|script|automation).*");

    @Override
    public double analyze(XAccount account) {
        if (account.getRecentTweets() == null || account.getRecentTweets().isEmpty()) {
            return 0.0;
        }

        double score = 0.0;
        int signals = 0;

        List<XTweet> tweets = account.getRecentTweets();

        // Signal 1: Only retweets, no original content
        long retweetCount = tweets.stream().filter(XTweet::isRetweet).count();
        double retweetRatio = (double) retweetCount / tweets.size();
        if (retweetRatio > 0.9) {
            score += 0.30; // Mostly retweets
        } else if (retweetRatio > 0.7) {
            score += 0.15;
        }
        signals++;

        // Signal 2: No replies (no engagement)
        long replyCount = tweets.stream().filter(XTweet::isReply).count();
        if (replyCount == 0 && tweets.size() > 10) {
            score += 0.25;
            signals++;
        }

        // Signal 3: No media attachments
        long mediaCount = tweets.stream().filter(XTweet::isHasMedia).count();
        if (mediaCount == 0 && tweets.size() > 20) {
            score += 0.15; // Bots often don't use media
            signals++;
        }

        // Signal 4: Suspicious client name
        boolean hasSuspiciousClient = tweets.stream()
            .anyMatch(tweet -> {
                String source = tweet.getSource();
                return source != null && API_CLIENT.matcher(source).matches();
            });
        if (hasSuspiciousClient) {
            score += 0.35;
            signals++;
        }

        // Signal 5: All tweets same language (no variation)
        boolean allSameLanguage = tweets.stream()
            .map(XTweet::getLanguage)
            .distinct()
            .count() == 1;
        if (allSameLanguage && tweets.size() > 10) {
            score += 0.10; // Slight bot indicator
            signals++;
        }

        return signals > 0 ? Math.max(0.0, Math.min(1.0, score)) : 0.0;
    }

    @Override
    public double getWeight() {
        return WEIGHT;
    }

    @Override
    public String getName() {
        return "BehaviorAnalyzer";
    }

    @Override
    public Map<String, Object> getSignalDetails(XAccount account) {
        Map<String, Object> details = new HashMap<>();

        if (account.getRecentTweets() == null || account.getRecentTweets().isEmpty()) {
            return details;
        }

        List<XTweet> tweets = account.getRecentTweets();

        long retweetCount = tweets.stream().filter(XTweet::isRetweet).count();
        long replyCount = tweets.stream().filter(XTweet::isReply).count();
        long mediaCount = tweets.stream().filter(XTweet::isHasMedia).count();

        details.put("retweet_ratio", (double) retweetCount / tweets.size());
        details.put("reply_count", replyCount);
        details.put("media_count", mediaCount);
        details.put("has_suspicious_client", tweets.stream()
            .anyMatch(tweet -> {
                String source = tweet.getSource();
                return source != null && API_CLIENT.matcher(source).matches();
            }));
        details.put("language_diversity", tweets.stream()
            .map(XTweet::getLanguage)
            .distinct()
            .count());

        return details;
    }
}

