package com.aihoneypot.xdetector.analyzer;

import com.aihoneypot.xdetector.model.XAccount;
import com.aihoneypot.xdetector.model.XTweet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Analyzes text content for AI-generated patterns.
 * Weight: 30% (highest weight)
 */
@Slf4j
@Component
public class TextAnalyzer implements SignalAnalyzer {

    private static final double WEIGHT = 0.30;

    // Common AI/LLM patterns
    private static final Pattern[] AI_PATTERNS = {
        Pattern.compile("(?i)as an ai|i'm an ai|i am an ai"),
        Pattern.compile("(?i)i don't have personal|i cannot|i'm unable to"),
        Pattern.compile("(?i)my programming|my training data"),
        Pattern.compile("(?i)i'm here to help|happy to assist|i'd be happy to"),
        Pattern.compile("(?i)here are \\d+ (ways|tips|reasons|ideas)"),
        Pattern.compile("(?i)it's important to note|it's worth noting"),
        Pattern.compile("(?i)in conclusion|to summarize|in summary")
    };

    // Automation tool clients
    private static final Set<String> AUTOMATION_CLIENTS = Set.of(
        "Buffer", "Hootsuite", "IFTTT", "Zapier", "dlvr.it",
        "Sprout Social", "SocialFlow", "CoSchedule"
    );

    @Override
    public double analyze(XAccount account) {
        if (account.getRecentTweets() == null || account.getRecentTweets().isEmpty()) {
            return 0.0;
        }

        double score = 0.0;
        int signals = 0;

        List<XTweet> tweets = account.getRecentTweets();

        // Signal 1: AI language patterns
        double aiPatternRatio = calculateAIPatternRatio(tweets);
        if (aiPatternRatio > 0.3) {
            score += 0.40; // High AI language usage
        } else if (aiPatternRatio > 0.1) {
            score += 0.20;
        }
        signals++;

        // Signal 2: Text repetition/similarity
        double similarityScore = calculateTextSimilarity(tweets);
        if (similarityScore > 0.8) {
            score += 0.35; // Very repetitive content
        } else if (similarityScore > 0.6) {
            score += 0.20;
        }
        signals++;

        // Signal 3: Automation client usage
        if (usesAutomationClient(tweets)) {
            score += 0.25;
            signals++;
        }

        // Signal 4: Low vocabulary diversity (Type-Token Ratio)
        double ttr = calculateTypeTokenRatio(tweets);
        if (ttr < 0.3) {
            score += 0.20; // Very low vocabulary diversity
        } else if (ttr < 0.5) {
            score += 0.10;
        }
        signals++;

        // Signal 5: Overly formal/perfect grammar (GPT-like)
        double formalityScore = calculateFormalityScore(tweets);
        if (formalityScore > 0.7) {
            score += 0.15;
        }
        signals++;

        return signals > 0 ? Math.max(0.0, Math.min(1.0, score)) : 0.0;
    }

    private double calculateAIPatternRatio(List<XTweet> tweets) {
        long matchCount = tweets.stream()
            .filter(tweet -> {
                String text = tweet.getText();
                for (Pattern pattern : AI_PATTERNS) {
                    if (pattern.matcher(text).find()) {
                        return true;
                    }
                }
                return false;
            })
            .count();

        return (double) matchCount / tweets.size();
    }

    private double calculateTextSimilarity(List<XTweet> tweets) {
        if (tweets.size() < 2) return 0.0;

        List<String> texts = tweets.stream()
            .map(XTweet::getText)
            .collect(Collectors.toList());

        LevenshteinDistance levenshtein = new LevenshteinDistance();
        double totalSimilarity = 0.0;
        int comparisons = 0;

        for (int i = 0; i < texts.size() - 1; i++) {
            for (int j = i + 1; j < texts.size(); j++) {
                String text1 = texts.get(i);
                String text2 = texts.get(j);

                int distance = levenshtein.apply(text1, text2);
                int maxLen = Math.max(text1.length(), text2.length());

                if (maxLen > 0) {
                    double similarity = 1.0 - ((double) distance / maxLen);
                    totalSimilarity += similarity;
                    comparisons++;
                }
            }
        }

        return comparisons > 0 ? totalSimilarity / comparisons : 0.0;
    }

    private boolean usesAutomationClient(List<XTweet> tweets) {
        return tweets.stream()
            .anyMatch(tweet -> {
                String source = tweet.getSource();
                if (source == null) return false;
                return AUTOMATION_CLIENTS.stream()
                    .anyMatch(client -> source.toLowerCase().contains(client.toLowerCase()));
            });
    }

    private double calculateTypeTokenRatio(List<XTweet> tweets) {
        Set<String> uniqueWords = new HashSet<>();
        int totalWords = 0;

        for (XTweet tweet : tweets) {
            String[] words = tweet.getText().toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .split("\\s+");

            for (String word : words) {
                if (!word.isEmpty() && word.length() > 2) {
                    uniqueWords.add(word);
                    totalWords++;
                }
            }
        }

        return totalWords > 0 ? (double) uniqueWords.size() / totalWords : 0.0;
    }

    private double calculateFormalityScore(List<XTweet> tweets) {
        // Heuristic: count formal markers vs informal markers
        int formalMarkers = 0;
        int informalMarkers = 0;

        Pattern[] formalPatterns = {
            Pattern.compile("(?i)\\b(therefore|furthermore|moreover|however|nevertheless)\\b"),
            Pattern.compile("(?i)\\b(regarding|concerning|pertaining)\\b"),
            Pattern.compile("\\.$") // Ends with period
        };

        Pattern[] informalPatterns = {
            Pattern.compile("(?i)\\b(lol|omg|wtf|tbh|imho)\\b"),
            Pattern.compile("!{2,}"), // Multiple exclamation marks
            Pattern.compile("\\?{2,}") // Multiple question marks
        };

        for (XTweet tweet : tweets) {
            String text = tweet.getText();

            for (Pattern p : formalPatterns) {
                if (p.matcher(text).find()) formalMarkers++;
            }

            for (Pattern p : informalPatterns) {
                if (p.matcher(text).find()) informalMarkers++;
            }
        }

        int total = formalMarkers + informalMarkers;
        return total > 0 ? (double) formalMarkers / total : 0.5;
    }

    @Override
    public double getWeight() {
        return WEIGHT;
    }

    @Override
    public String getName() {
        return "TextAnalyzer";
    }

    @Override
    public Map<String, Object> getSignalDetails(XAccount account) {
        Map<String, Object> details = new HashMap<>();

        if (account.getRecentTweets() == null || account.getRecentTweets().isEmpty()) {
            return details;
        }

        List<XTweet> tweets = account.getRecentTweets();

        details.put("ai_pattern_ratio", calculateAIPatternRatio(tweets));
        details.put("text_similarity_score", calculateTextSimilarity(tweets));
        details.put("uses_automation_client", usesAutomationClient(tweets));
        details.put("type_token_ratio", calculateTypeTokenRatio(tweets));
        details.put("formality_score", calculateFormalityScore(tweets));

        return details;
    }
}

