package com.aihoneypot.xdetector.analyzer;

import com.aihoneypot.xdetector.model.XAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Analyzes follower/following ratios and engagement metrics.
 * Weight: 25%
 */
@Slf4j
@Component
public class NetworkAnalyzer implements SignalAnalyzer {

    private static final double WEIGHT = 0.25;

    @Override
    public double analyze(XAccount account) {
        double score = 0.0;
        int signals = 0;

        // Signal 1: Follower/Following ratio
        if (account.getFollowingCount() > 0) {
            double ratio = (double) account.getFollowersCount() / account.getFollowingCount();

            if (ratio < 0.1 && account.getFollowingCount() > 100) {
                // Following many, few followers = likely bot
                score += 0.35;
            } else if (ratio < 0.3) {
                score += 0.20;
            } else if (ratio > 10.0 && account.getFollowersCount() > 10000) {
                // Celebrity/influencer pattern (negative signal)
                score -= 0.10;
            }
            signals++;
        }

        // Signal 2: Following spam pattern
        if (account.getFollowingCount() > 5000) {
            score += 0.25;
            signals++;
        }

        // Signal 3: Zero followers (brand new account)
        if (account.getFollowersCount() == 0 && account.getTweetCount() > 10) {
            score += 0.30; // Active but no followers = suspicious
            signals++;
        }

        // Signal 4: Engagement ratio (tweets vs likes)
        if (account.getTweetCount() > 0) {
            double likesPerTweet = (double) account.getLikesCount() / account.getTweetCount();
            if (likesPerTweet < 0.1 && account.getTweetCount() > 50) {
                // Many tweets but no likes = no engagement
                score += 0.25;
            }
            signals++;
        }

        // Signal 5: High tweet count with low followers
        if (account.getTweetCount() > 1000 && account.getFollowersCount() < 100) {
            score += 0.30;
            signals++;
        }

        // Normalize
        return signals > 0 ? Math.max(0.0, Math.min(1.0, score)) : 0.0;
    }

    @Override
    public double getWeight() {
        return WEIGHT;
    }

    @Override
    public String getName() {
        return "NetworkAnalyzer";
    }

    @Override
    public Map<String, Object> getSignalDetails(XAccount account) {
        Map<String, Object> details = new HashMap<>();

        double followerRatio = account.getFollowingCount() > 0
            ? (double) account.getFollowersCount() / account.getFollowingCount()
            : 0.0;

        details.put("follower_following_ratio", followerRatio);
        details.put("followers_count", account.getFollowersCount());
        details.put("following_count", account.getFollowingCount());
        details.put("tweet_count", account.getTweetCount());
        details.put("likes_count", account.getLikesCount());

        double likesPerTweet = account.getTweetCount() > 0
            ? (double) account.getLikesCount() / account.getTweetCount()
            : 0.0;
        details.put("likes_per_tweet", likesPerTweet);

        details.put("is_follow_spam", account.getFollowingCount() > 5000);
        details.put("zero_engagement", account.getFollowersCount() == 0 && account.getTweetCount() > 10);

        return details;
    }
}

