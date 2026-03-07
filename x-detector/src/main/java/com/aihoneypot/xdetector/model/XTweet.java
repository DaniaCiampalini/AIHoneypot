package com.aihoneypot.xdetector.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Represents a single tweet/post from X.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XTweet {

    /**
     * Tweet ID
     */
    private String tweetId;

    /**
     * Tweet text content
     */
    private String text;

    /**
     * Tweet creation timestamp
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;

    /**
     * Retweet count
     */
    private int retweetCount;

    /**
     * Like count
     */
    private int likeCount;

    /**
     * Reply count
     */
    private int replyCount;

    /**
     * Quote count
     */
    private int quoteCount;

    /**
     * Is this a retweet?
     */
    private boolean isRetweet;

    /**
     * Is this a reply?
     */
    private boolean isReply;

    /**
     * Is this a quote tweet?
     */
    private boolean isQuote;

    /**
     * Client/source used to post (e.g., "Twitter Web App", "Buffer", etc.)
     */
    private String source;

    /**
     * Language of the tweet
     */
    private String language;

    /**
     * Contains media (images, videos)?
     */
    private boolean hasMedia;
}

