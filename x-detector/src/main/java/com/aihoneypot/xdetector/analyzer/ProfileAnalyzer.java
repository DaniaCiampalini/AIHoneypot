package com.aihoneypot.xdetector.analyzer;

import com.aihoneypot.xdetector.model.XAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Analyzes static profile data for bot signals.
 * Weight: 20%
 */
@Slf4j
@Component
public class ProfileAnalyzer implements SignalAnalyzer {

    private static final double WEIGHT = 0.20;
    private static final Pattern NUMERIC_USERNAME = Pattern.compile(".*\\d{4,}.*");
    private static final Pattern[] AI_KEYWORDS = {
        Pattern.compile("(?i).*(gpt|ai bot|automated|llm|assistant).*"),
        Pattern.compile("(?i).*(powered by|built with|using).*(openai|anthropic|claude).*")
    };

    @Override
    public double analyze(XAccount account) {
        double score = 0.0;
        int signals = 0;

        // Signal 1: Account age (newer = more suspicious)
        if (account.getCreatedAt() != null) {
            long daysOld = Duration.between(account.getCreatedAt(), Instant.now()).toDays();
            if (daysOld < 7) {
                score += 0.30; // Very new account
            } else if (daysOld < 30) {
                score += 0.15;
            } else if (daysOld < 90) {
                score += 0.05;
            }
            signals++;
        }

        // Signal 2: Username pattern (numeric suffix)
        if (account.getUsername() != null && NUMERIC_USERNAME.matcher(account.getUsername()).matches()) {
            score += 0.25;
            signals++;
        }

        // Signal 3: AI keywords in bio
        if (account.getBio() != null) {
            for (Pattern pattern : AI_KEYWORDS) {
                if (pattern.matcher(account.getBio()).matches()) {
                    score += 0.30;
                    break;
                }
            }
            signals++;
        }

        // Signal 4: Default profile image
        if (account.isDefaultProfileImage()) {
            score += 0.20;
            signals++;
        }

        // Signal 5: No bio
        if (account.getBio() == null || account.getBio().trim().isEmpty()) {
            score += 0.15;
            signals++;
        }

        // Signal 6: Verified account (negative signal - reduces bot score)
        if (account.isVerified()) {
            score -= 0.50; // Strong human signal
            signals++;
        }

        // Normalize score to 0-1 range
        return signals > 0 ? Math.max(0.0, Math.min(1.0, score)) : 0.0;
    }

    @Override
    public double getWeight() {
        return WEIGHT;
    }

    @Override
    public String getName() {
        return "ProfileAnalyzer";
    }

    @Override
    public Map<String, Object> getSignalDetails(XAccount account) {
        Map<String, Object> details = new HashMap<>();

        if (account.getCreatedAt() != null) {
            long daysOld = Duration.between(account.getCreatedAt(), Instant.now()).toDays();
            details.put("account_age_days", daysOld);
            details.put("is_new_account", daysOld < 30);
        }

        details.put("has_numeric_username",
            account.getUsername() != null && NUMERIC_USERNAME.matcher(account.getUsername()).matches());

        boolean hasAIKeywords = false;
        if (account.getBio() != null) {
            for (Pattern pattern : AI_KEYWORDS) {
                if (pattern.matcher(account.getBio()).matches()) {
                    hasAIKeywords = true;
                    break;
                }
            }
        }
        details.put("has_ai_keywords_in_bio", hasAIKeywords);
        details.put("has_default_profile_image", account.isDefaultProfileImage());
        details.put("has_empty_bio", account.getBio() == null || account.getBio().trim().isEmpty());
        details.put("is_verified", account.isVerified());

        return details;
    }
}

