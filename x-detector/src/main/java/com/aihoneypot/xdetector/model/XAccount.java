package com.aihoneypot.xdetector.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Represents an X (Twitter) account with all relevant data for bot detection.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XAccount {

    /**
     * Twitter user ID
     */
    private String userId;

    /**
     * Twitter username (without @)
     */
    private String username;

    /**
     * Display name
     */
    private String displayName;

    /**
     * Account bio/description
     */
    private String bio;

    /**
     * Profile image URL
     */
    private String profileImageUrl;

    /**
     * Is verified account?
     */
    private boolean verified;

    /**
     * Account creation date
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;

    /**
     * Follower count
     */
    private int followersCount;

    /**
     * Following count
     */
    private int followingCount;

    /**
     * Total tweets count
     */
    private int tweetCount;

    /**
     * Total likes count
     */
    private int likesCount;

    /**
     * Location (if provided)
     */
    private String location;

    /**
     * Website URL (if provided)
     */
    private String url;

    /**
     * Recent tweets (for analysis)
     */
    private List<XTweet> recentTweets;

    /**
     * Has default profile image?
     */
    private boolean defaultProfileImage;

    /**
     * Is protected account?
     */
    private boolean isProtected;
}


