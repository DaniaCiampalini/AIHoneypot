package com.aihoneypot.xdetector.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for manual input of X account data (when API access is not available).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManualAccountInput {

    private String username;
    private String displayName;
    private String bio;
    private boolean verified;
    private String accountCreatedDate; // ISO 8601 format
    private int followersCount;
    private int followingCount;
    private int tweetCount;
    private boolean defaultProfileImage;

    /**
     * Optional: recent tweet texts for AI detection
     */
    private List<String> recentTweetTexts;

    /**
     * Optional: recent tweet timestamps (ISO 8601)
     */
    private List<String> recentTweetTimestamps;

    /**
     * Optional: tweet sources/clients
     */
    private List<String> tweetSources;
}

