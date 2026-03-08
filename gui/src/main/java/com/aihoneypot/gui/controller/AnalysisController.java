package com.aihoneypot.gui.controller;

import com.aihoneypot.gui.service.DashboardApiService;
import com.aihoneypot.gui.service.SecurityAnalysisService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for Direct Analysis Panel.
 */
public class AnalysisController {

    @FXML private ComboBox<String> analysisTypeCombo;
    @FXML private TextField inputField;
    @FXML private TextArea detailsArea;
    @FXML private Button analyzeButton;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label resultLabel;
    @FXML private VBox resultPane;

    private DashboardApiService apiService;
    private SecurityAnalysisService securityService;

    @FXML
    private void initialize() {
        // Initialize security service
        securityService = new SecurityAnalysisService();

        // Setup analysis types
        analysisTypeCombo.getItems().addAll(
            "🌐 Website/URL",
            "🐦 X (Twitter) Account",
            "📧 Email Address",
            "🌍 IP Address",
            "👤 User Agent String"
        );
        analysisTypeCombo.setValue("🌐 Website/URL");

        // Initially hide result pane
        resultPane.setVisible(false);
        loadingIndicator.setVisible(false);

        // Update placeholder based on selection
        analysisTypeCombo.setOnAction(e -> updatePlaceholder());
        updatePlaceholder();
    }

    public void setApiService(DashboardApiService apiService) {
        this.apiService = apiService;
    }

    private void updatePlaceholder() {
        String selected = analysisTypeCombo.getValue();

        if (selected.contains("Website")) {
            inputField.setPromptText("Enter URL (e.g., https://example.com)");
            detailsArea.setPromptText("Optional: Add additional context or notes...");
        } else if (selected.contains("Twitter")) {
            inputField.setPromptText("Enter username (e.g., @username or username)");
            detailsArea.setPromptText("Optional: Add recent tweets or bio information...");
        } else if (selected.contains("Email")) {
            inputField.setPromptText("Enter email address (e.g., user@example.com)");
            detailsArea.setPromptText("Optional: Add email headers or content...");
        } else if (selected.contains("IP")) {
            inputField.setPromptText("Enter IP address (e.g., 192.168.1.1)");
            detailsArea.setPromptText("Optional: Add port numbers or service information...");
        } else if (selected.contains("User Agent")) {
            inputField.setPromptText("Enter User-Agent string");
            detailsArea.setPromptText("Optional: Add additional HTTP headers...");
        }
    }

    @FXML
    private void handleAnalyze() {
        String input = inputField.getText().trim();
        String details = detailsArea.getText().trim();

        if (input.isEmpty()) {
            showError("Please enter something to analyze");
            return;
        }

        // Show loading
        loadingIndicator.setVisible(true);
        analyzeButton.setDisable(true);
        resultPane.setVisible(false);

        // Perform analysis in background
        new Thread(() -> {
            try {
                Map<String, Object> result = performAnalysis(input, details);

                Platform.runLater(() -> {
                    displayResult(result);
                    loadingIndicator.setVisible(false);
                    analyzeButton.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Analysis failed: " + e.getMessage());
                    loadingIndicator.setVisible(false);
                    analyzeButton.setDisable(false);
                });
            }
        }).start();
    }

    private Map<String, Object> performAnalysis(String input, String details) {
        String type = analysisTypeCombo.getValue();
        Map<String, Object> result = new HashMap<>();

        // Simulate analysis (replace with actual API calls)
        try {
            Thread.sleep(1000); // Simulate processing time

            if (type.contains("Website")) {
                result = analyzeWebsite(input, details);
            } else if (type.contains("Twitter")) {
                result = analyzeTwitterAccount(input, details);
            } else if (type.contains("Email")) {
                result = analyzeEmail(input, details);
            } else if (type.contains("IP")) {
                result = analyzeIP(input, details);
            } else if (type.contains("User Agent")) {
                result = analyzeUserAgent(input, details);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return result;
    }

    private Map<String, Object> analyzeWebsite(String url, String details) {
        // Use advanced security analysis
        Map<String, Object> securityAnalysis = securityService.analyzeWebsiteSecurity(url);

        Map<String, Object> result = new HashMap<>();
        result.put("type", "Website Security Analysis");
        result.put("input", url);

        double score = (Double) securityAnalysis.get("score");
        String level = (String) securityAnalysis.get("level");

        // Map security level to threat level
        String threatLevel;
        if (level.equals("SECURE")) {
            threatLevel = "LOW";
        } else if (level.equals("MODERATE")) {
            threatLevel = "MEDIUM";
        } else {
            threatLevel = "HIGH";
        }

        result.put("threat_level", threatLevel);
        result.put("is_bot", false);
        result.put("is_ai_generated", false);
        result.put("confidence", score / 100.0);

        // Build detailed explanation
        StringBuilder explanation = new StringBuilder();
        explanation.append("Security Score: ").append(String.format("%.1f/100", score)).append("\n\n");

        @SuppressWarnings("unchecked")
        List<String> vulnerabilities = (List<String>) securityAnalysis.get("vulnerabilities");
        if (!vulnerabilities.isEmpty()) {
            explanation.append("Vulnerabilities Found:\n");
            for (String vuln : vulnerabilities) {
                explanation.append("  ").append(vuln).append("\n");
            }
            explanation.append("\n");
        }

        @SuppressWarnings("unchecked")
        List<String> warnings = (List<String>) securityAnalysis.get("warnings");
        if (!warnings.isEmpty()) {
            explanation.append("Warnings:\n");
            for (String warn : warnings) {
                explanation.append("  ").append(warn).append("\n");
            }
            explanation.append("\n");
        }

        @SuppressWarnings("unchecked")
        List<String> goodPractices = (List<String>) securityAnalysis.get("good_practices");
        if (!goodPractices.isEmpty()) {
            explanation.append("Good Practices:\n");
            for (String practice : goodPractices) {
                explanation.append("  ").append(practice).append("\n");
            }
        }

        result.put("explanation", explanation.toString());
        result.put("security_details", securityAnalysis);

        return result;
    }

    private Map<String, Object> analyzeTwitterAccount(String username, String details) {
        Map<String, Object> result = new HashMap<>();

        // Remove @ if present
        username = username.replace("@", "");

        // Simple heuristics
        boolean hasNumbers = username.matches(".*\\d{4,}.*");
        boolean hasAIKeywords = details.toLowerCase().contains("ai") ||
                                details.toLowerCase().contains("bot") ||
                                details.toLowerCase().contains("gpt");

        double confidence = 0.5;
        if (hasNumbers) confidence += 0.2;
        if (hasAIKeywords) confidence += 0.3;

        result.put("type", "Twitter Account Analysis");
        result.put("input", "@" + username);
        result.put("threat_level", confidence > 0.6 ? "MEDIUM" : "LOW");
        result.put("is_bot", confidence > 0.6);
        result.put("is_ai_generated", hasAIKeywords);
        result.put("confidence", confidence);
        result.put("explanation", String.format(
            "Account shows %s indicators of automated behavior. " +
            "Username pattern: %s, Content analysis: %s",
            confidence > 0.6 ? "several" : "few",
            hasNumbers ? "numeric suffix detected" : "normal",
            hasAIKeywords ? "AI-related keywords found" : "appears human"
        ));

        return result;
    }

    private Map<String, Object> analyzeEmail(String email, String details) {
        Map<String, Object> result = new HashMap<>();

        boolean suspicious = email.contains("noreply") || email.contains("bot") ||
                           email.contains("automated");

        result.put("type", "Email Analysis");
        result.put("input", email);
        result.put("threat_level", suspicious ? "MEDIUM" : "LOW");
        result.put("is_bot", suspicious);
        result.put("is_ai_generated", false);
        result.put("confidence", suspicious ? 0.6 : 0.3);
        result.put("explanation", suspicious
            ? "Email address contains automated service indicators"
            : "Email address appears to be from a human user");

        return result;
    }

    private Map<String, Object> analyzeIP(String ip, String details) {
        Map<String, Object> result = new HashMap<>();

        // Check if it's a known bot pattern
        boolean suspicious = ip.startsWith("192.168") || ip.startsWith("10.") ||
                           details.toLowerCase().contains("scanner");

        result.put("type", "IP Address Analysis");
        result.put("input", ip);
        result.put("threat_level", suspicious ? "MEDIUM" : "LOW");
        result.put("is_bot", suspicious);
        result.put("is_ai_generated", false);
        result.put("confidence", suspicious ? 0.55 : 0.25);
        result.put("explanation", suspicious
            ? "IP shows patterns associated with automated scanning"
            : "IP appears to be from a normal client");

        return result;
    }

    private Map<String, Object> analyzeUserAgent(String userAgent, String details) {
        Map<String, Object> result = new HashMap<>();

        boolean isBot = userAgent.toLowerCase().contains("bot") ||
                       userAgent.toLowerCase().contains("crawler") ||
                       userAgent.toLowerCase().contains("spider");

        boolean isAI = userAgent.toLowerCase().contains("gpt") ||
                      userAgent.toLowerCase().contains("claude") ||
                      userAgent.toLowerCase().contains("ai");

        double confidence = 0.3;
        if (isBot) confidence = 0.8;
        if (isAI) confidence = 0.9;

        String threatLevel = confidence > 0.7 ? "HIGH" : (confidence > 0.4 ? "MEDIUM" : "LOW");

        result.put("type", "User-Agent Analysis");
        result.put("input", userAgent);
        result.put("threat_level", threatLevel);
        result.put("is_bot", isBot || isAI);
        result.put("is_ai_generated", isAI);
        result.put("confidence", confidence);
        result.put("explanation", String.format(
            "User-Agent analysis: %s. Classification: %s",
            isAI ? "AI agent detected" : (isBot ? "Bot/crawler detected" : "Appears to be a browser"),
            isAI ? "AI_AGENT" : (isBot ? "BOT" : "HUMAN_BROWSER")
        ));

        return result;
    }

    private void displayResult(Map<String, Object> result) {
        StringBuilder sb = new StringBuilder();

        sb.append("═══════════════════════════════════════\n");
        sb.append("  ANALYSIS RESULT\n");
        sb.append("═══════════════════════════════════════\n\n");

        sb.append("Type: ").append(result.get("type")).append("\n");
        sb.append("Input: ").append(result.get("input")).append("\n\n");

        String threatLevel = (String) result.get("threat_level");
        sb.append("Threat Level: ");
        switch (threatLevel) {
            case "HIGH":
                sb.append("🔴 HIGH");
                break;
            case "MEDIUM":
                sb.append("🟡 MEDIUM");
                break;
            default:
                sb.append("🟢 LOW");
        }
        sb.append("\n\n");

        boolean isBot = (Boolean) result.get("is_bot");
        boolean isAI = (Boolean) result.get("is_ai_generated");
        double confidence = (Double) result.get("confidence");

        sb.append("Is Bot: ").append(isBot ? "✅ YES" : "❌ NO").append("\n");
        sb.append("Is AI Generated: ").append(isAI ? "✅ YES" : "❌ NO").append("\n");
        sb.append("Confidence: ").append(String.format("%.1f%%", confidence * 100)).append("\n\n");

        sb.append("Explanation:\n");
        sb.append(result.get("explanation")).append("\n\n");

        sb.append("═══════════════════════════════════════\n");

        resultLabel.setText(sb.toString());
        resultPane.setVisible(true);

        // Set color based on threat level
        if ("HIGH".equals(threatLevel)) {
            resultLabel.setStyle("-fx-text-fill: #f85149;");
        } else if ("MEDIUM".equals(threatLevel)) {
            resultLabel.setStyle("-fx-text-fill: #e3b341;");
        } else {
            resultLabel.setStyle("-fx-text-fill: #56d364;");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleClear() {
        inputField.clear();
        detailsArea.clear();
        resultPane.setVisible(false);
    }
}

