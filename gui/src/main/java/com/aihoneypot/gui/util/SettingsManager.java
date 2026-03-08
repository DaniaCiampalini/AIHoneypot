package com.aihoneypot.gui.util;

import javafx.scene.Scene;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Manages application settings including theme preference.
 */
public class SettingsManager {

    private static final String SETTINGS_FILE = System.getProperty("user.home") + "/.aihoneypot/settings.properties";
    private static final String THEME_KEY = "theme";
    private static final String DEFAULT_THEME = "dark";

    private final Properties properties;
    private Scene scene;

    public SettingsManager() {
        this.properties = new Properties();
        loadSettings();
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    /**
     * Load settings from file.
     */
    private void loadSettings() {
        try {
            java.io.File file = new java.io.File(SETTINGS_FILE);
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    properties.load(fis);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load settings: " + e.getMessage());
        }
    }

    /**
     * Save settings to file.
     */
    private void saveSettings() {
        try {
            java.io.File file = new java.io.File(SETTINGS_FILE);
            file.getParentFile().mkdirs();

            try (FileOutputStream fos = new FileOutputStream(file)) {
                properties.store(fos, "AIHoneypot GUI Settings");
            }
        } catch (IOException e) {
            System.err.println("Could not save settings: " + e.getMessage());
        }
    }

    /**
     * Get current theme.
     */
    public String getTheme() {
        return properties.getProperty(THEME_KEY, DEFAULT_THEME);
    }

    /**
     * Set theme and apply it.
     */
    public void setTheme(String theme) {
        properties.setProperty(THEME_KEY, theme);
        saveSettings();

        if (scene != null) {
            applyTheme();
        }
    }

    /**
     * Apply current theme to scene.
     */
    public void applyTheme() {
        if (scene == null) return;

        scene.getStylesheets().clear();

        String cssFile;
        String theme = getTheme();

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

        try {
            scene.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
            System.out.println("Theme applied: " + getTheme());
        } catch (Exception e) {
            System.err.println("Could not load theme: " + e.getMessage());
        }
    }

    /**
     * Toggle between light and dark theme.
     */
    public void toggleTheme() {
        String currentTheme = getTheme();
        String newTheme = currentTheme.equals("dark") ? "light" : "dark";
        setTheme(newTheme);
    }
}

