package com.aihoneypot.gui.controller;

import com.aihoneypot.dashboard.dto.ThreatSessionDTO;
import com.aihoneypot.gui.service.DashboardApiService;
import com.aihoneypot.gui.util.SettingsManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Main dashboard controller for JavaFX GUI.
 */
public class MainDashboardController {

    // Service
    private DashboardApiService apiService;
    private Timeline refreshTimeline;
    private SettingsManager settingsManager;

    // Top statistics
    @FXML private Label totalThreatsLabel;
    @FXML private Label totalSessionsLabel;
    @FXML private Label activeSessionsLabel;
    @FXML private Label canaryHitsLabel;

    // Charts
    @FXML private PieChart clientTypePieChart;
    @FXML private BarChart<String, Number> severityBarChart;
    @FXML private LineChart<String, Number> timelineChart;

    // Tables
    @FXML private TableView<ThreatSessionDTO> threatsTable;
    @FXML private TableColumn<ThreatSessionDTO, String> sessionIdColumn;
    @FXML private TableColumn<ThreatSessionDTO, String> ipAddressColumn;
    @FXML private TableColumn<ThreatSessionDTO, String> clientTypeColumn;
    @FXML private TableColumn<ThreatSessionDTO, String> severityColumn;
    @FXML private TableColumn<ThreatSessionDTO, Double> confidenceColumn;
    @FXML private TableColumn<ThreatSessionDTO, String> timestampColumn;

    @FXML private TableView<Map<String, Object>> topIPsTable;
    @FXML private TableColumn<Map<String, Object>, String> ipColumn;
    @FXML private TableColumn<Map<String, Object>, Long> countColumn;

    // Status
    @FXML private Label statusLabel;
    @FXML private Label lastUpdateLabel;
    @FXML private ProgressIndicator loadingIndicator;

    // Controls
    @FXML private Button refreshButton;
    @FXML private CheckBox autoRefreshCheckBox;
    @FXML private ComboBox<Integer> refreshIntervalComboBox;

    @FXML
    public void initialize() {
        // Initialize API service
        apiService = new DashboardApiService("http://localhost:8080");

        // Setup table columns
        setupTables();

        // Setup refresh controls
        setupRefreshControls();

        // Initial data load
        refreshData();

        // Check API health
        checkApiHealth();
    }

    private void setupTables() {
        // Threats table
        sessionIdColumn.setCellValueFactory(new PropertyValueFactory<>("sessionId"));
        ipAddressColumn.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        clientTypeColumn.setCellValueFactory(new PropertyValueFactory<>("clientType"));
        severityColumn.setCellValueFactory(new PropertyValueFactory<>("severity"));
        confidenceColumn.setCellValueFactory(new PropertyValueFactory<>("confidence"));
        timestampColumn.setCellValueFactory(cellData -> {
            Instant timestamp = cellData.getValue().getTimestamp();
            if (timestamp != null) {
                LocalDateTime ldt = LocalDateTime.ofInstant(timestamp, ZoneId.systemDefault());
                return new javafx.beans.property.SimpleStringProperty(
                    ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        // Format confidence as percentage
        confidenceColumn.setCellFactory(column -> new TableCell<ThreatSessionDTO, Double>() {
            @Override
            protected void updateItem(Double confidence, boolean empty) {
                super.updateItem(confidence, empty);
                if (empty || confidence == null) {
                    setText(null);
                } else {
                    setText(String.format("%.1f%%", confidence * 100));
                }
            }
        });

        // Color code severity
        severityColumn.setCellFactory(column -> new TableCell<ThreatSessionDTO, String>() {
            @Override
            protected void updateItem(String severity, boolean empty) {
                super.updateItem(severity, empty);
                if (empty || severity == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(severity);
                    switch (severity) {
                        case "CRITICAL":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                        case "HIGH":
                            setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
                            break;
                        case "MEDIUM":
                            setStyle("-fx-text-fill: #f39c12;");
                            break;
                        case "LOW":
                            setStyle("-fx-text-fill: #95a5a6;");
                            break;
                    }
                }
            }
        });

        // Top IPs table
        ipColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty((String) cellData.getValue().get("ipAddress"))
        );
        countColumn.setCellValueFactory(cellData -> {
            Object count = cellData.getValue().get("count");
            return new javafx.beans.property.SimpleObjectProperty<>((Long) count);
        });
    }

    private void setupRefreshControls() {
        refreshIntervalComboBox.setItems(FXCollections.observableArrayList(5, 10, 30, 60));
        refreshIntervalComboBox.setValue(10);

        autoRefreshCheckBox.setSelected(true);
        startAutoRefresh();

        autoRefreshCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                startAutoRefresh();
            } else {
                stopAutoRefresh();
            }
        });

        refreshButton.setOnAction(event -> refreshData());
    }

    @FXML
    private void refreshData() {
        loadingIndicator.setVisible(true);

        new Thread(() -> {
            try {
                // Load statistics
                Map<String, Object> stats = apiService.getStatistics();

                // Load recent threats
                List<ThreatSessionDTO> threats = apiService.getRecentThreats(50);

                // Load top IPs
                List<Map<String, Object>> topIPs = apiService.getTopAttackingIPs(10);

                // Load chart data
                Map<String, Long> clientTypeStats = apiService.getThreatsByClientType();
                Map<String, Long> severityStats = apiService.getThreatsBySeverity();

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    updateStatistics(stats);
                    updateThreatsTable(threats);
                    updateTopIPsTable(topIPs);
                    updateClientTypeChart(clientTypeStats);
                    updateSeverityChart(severityStats);

                    lastUpdateLabel.setText("Last update: " +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    loadingIndicator.setVisible(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Error: " + e.getMessage());
                    loadingIndicator.setVisible(false);
                });
            }
        }).start();
    }

    private void updateStatistics(Map<String, Object> stats) {
        totalThreatsLabel.setText(String.valueOf(stats.getOrDefault("totalThreats", 0)));
        totalSessionsLabel.setText(String.valueOf(stats.getOrDefault("totalSessions", 0)));
        activeSessionsLabel.setText(String.valueOf(stats.getOrDefault("activeSessions", 0)));
        canaryHitsLabel.setText(String.valueOf(stats.getOrDefault("canaryTrapHits", 0)));
    }

    private void updateThreatsTable(List<ThreatSessionDTO> threats) {
        ObservableList<ThreatSessionDTO> data = FXCollections.observableArrayList(threats);
        threatsTable.setItems(data);
    }

    private void updateTopIPsTable(List<Map<String, Object>> topIPs) {
        ObservableList<Map<String, Object>> data = FXCollections.observableArrayList(topIPs);
        topIPsTable.setItems(data);
    }

    private void updateClientTypeChart(Map<String, Long> stats) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        stats.forEach((type, count) -> {
            if (count > 0) {
                pieData.add(new PieChart.Data(type, count));
            }
        });
        clientTypePieChart.setData(pieData);
    }

    private void updateSeverityChart(Map<String, Long> stats) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Threats by Severity");

        stats.forEach((severity, count) -> {
            series.getData().add(new XYChart.Data<>(severity, count));
        });

        severityBarChart.getData().clear();
        severityBarChart.getData().add(series);
    }

    private void checkApiHealth() {
        new Thread(() -> {
            boolean healthy = apiService.isHealthy();
            Platform.runLater(() -> {
                if (healthy) {
                    statusLabel.setText("✓ Connected to " + apiService.getBaseUrl());
                    statusLabel.setStyle("-fx-text-fill: #2ecc71;");
                } else {
                    statusLabel.setText("✗ Cannot connect to " + apiService.getBaseUrl());
                    statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                }
            });
        }).start();
    }

    private void startAutoRefresh() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }

        int interval = refreshIntervalComboBox.getValue();
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(interval), event -> refreshData()));
        refreshTimeline.setCycleCount(Animation.INDEFINITE);
        refreshTimeline.play();
    }

    private void stopAutoRefresh() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
    }

    public void setSettingsManager(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    @FXML
    private void handleSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/settings-dialog.fxml"));
            Parent root = loader.load();

            SettingsController controller = loader.getController();
            controller.setSettingsManager(settingsManager);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Settings");
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root, 400, 350);
            // Apply current theme to dialog
            if (settingsManager != null) {
                String theme = settingsManager.getTheme();
                String cssFile;
                switch (theme) {
                    case "light":
                        cssFile = "/css/light-theme.css";
                        break;
                    case "ios":
                        cssFile = "/css/ios-theme.css";
                        break;
                    default:
                        cssFile = "/css/dark-theme.css";
                }
                scene.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
            }

            dialogStage.setScene(scene);
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not open settings dialog: " + e.getMessage());
        }
    }
}

