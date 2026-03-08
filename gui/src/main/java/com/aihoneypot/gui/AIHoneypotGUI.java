package com.aihoneypot.gui;

import com.aihoneypot.gui.controller.MainDashboardController;
import com.aihoneypot.gui.util.SettingsManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Main JavaFX Application for AIHoneypot Dashboard.
 */
public class AIHoneypotGUI extends Application {

    private static final String TITLE = "AIHoneypot Dashboard";
    private static final int WIDTH = 1400;
    private static final int HEIGHT = 900;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize settings manager
        SettingsManager settingsManager = new SettingsManager();

        // Load FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-dashboard.fxml"));
        Parent root = loader.load();

        // Get controller and inject settings manager
        MainDashboardController controller = loader.getController();
        controller.setSettingsManager(settingsManager);

        // Create scene with initial size
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        // Set scene to settings manager and apply theme
        settingsManager.setScene(scene);
        settingsManager.applyTheme();

        // Configure stage for modern app look
        primaryStage.setTitle(TITLE);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);

        // Center on screen
        primaryStage.centerOnScreen();

        // Set icon (if available)
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        } catch (Exception e) {
            // Icon not found, continue without it
            System.out.println("App icon not found, using default");
        }

        // Maximize on startup for better app experience
        primaryStage.setMaximized(true);

        primaryStage.show();

        System.out.println("AIHoneypot Dashboard started successfully!");
        System.out.println("Backend API: http://localhost:8080");
        System.out.println("Current theme: " + settingsManager.getTheme());
    }

    @Override
    public void stop() {
        // Cleanup resources
        System.out.println("AIHoneypot GUI shutting down...");
    }

    public static void main(String[] args) {
        launch(args);
    }
}

