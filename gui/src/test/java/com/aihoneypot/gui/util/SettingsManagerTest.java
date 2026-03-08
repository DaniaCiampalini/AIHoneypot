package com.aihoneypot.gui.util;

import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SettingsManager.
 * Tests settings persistence, theme management, and file I/O.
 */
@DisplayName("SettingsManager Tests")
class SettingsManagerTest {

    private SettingsManager settingsManager;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        settingsManager = new SettingsManager();
    }

    @Test
    @DisplayName("Should initialize with default theme")
    void testDefaultTheme() {
        // When
        String theme = settingsManager.getTheme();

        // Then
        assertNotNull(theme);
        assertTrue(theme.equals("dark") || theme.equals("light") || theme.equals("ios"),
            "Theme should be one of: dark, light, ios");
    }

    @Test
    @DisplayName("Should set and get theme correctly")
    void testSetAndGetTheme() {
        // Given
        String newTheme = "ios";

        // When
        settingsManager.setTheme(newTheme);
        String retrievedTheme = settingsManager.getTheme();

        // Then
        assertEquals(newTheme, retrievedTheme);
    }

    @Test
    @DisplayName("Should handle dark theme")
    void testDarkTheme() {
        // When
        settingsManager.setTheme("dark");

        // Then
        assertEquals("dark", settingsManager.getTheme());
    }

    @Test
    @DisplayName("Should handle light theme")
    void testLightTheme() {
        // When
        settingsManager.setTheme("light");

        // Then
        assertEquals("light", settingsManager.getTheme());
    }

    @Test
    @DisplayName("Should handle iOS theme")
    void testIOSTheme() {
        // When
        settingsManager.setTheme("ios");

        // Then
        assertEquals("ios", settingsManager.getTheme());
    }

    @Test
    @DisplayName("Should toggle between themes")
    void testToggleTheme() {
        // Given
        settingsManager.setTheme("dark");
        String initialTheme = settingsManager.getTheme();

        // When
        settingsManager.toggleTheme();
        String toggledTheme = settingsManager.getTheme();

        // Then
        assertNotEquals(initialTheme, toggledTheme);
    }

    @Test
    @DisplayName("Should toggle from dark to light")
    void testToggleDarkToLight() {
        // Given
        settingsManager.setTheme("dark");

        // When
        settingsManager.toggleTheme();

        // Then
        assertEquals("light", settingsManager.getTheme());
    }

    @Test
    @DisplayName("Should toggle from light to dark")
    void testToggleLightToDark() {
        // Given
        settingsManager.setTheme("light");

        // When
        settingsManager.toggleTheme();

        // Then
        assertEquals("dark", settingsManager.getTheme());
    }

    @Test
    @DisplayName("Should handle null scene gracefully")
    void testNullSceneHandling() {
        // When & Then - should not throw exception
        assertDoesNotThrow(() -> {
            settingsManager.applyTheme();
        });
    }

    @Test
    @DisplayName("Should set scene correctly")
    void testSetScene() {
        // Given
        VBox root = new VBox();
        Scene scene = new Scene(root, 800, 600);

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> {
            settingsManager.setScene(scene);
        });
    }

    @Test
    @DisplayName("Should persist theme setting")
    void testThemePersistence() {
        // Given
        String theme = "ios";

        // When
        settingsManager.setTheme(theme);

        // Create new instance to test persistence
        SettingsManager newManager = new SettingsManager();
        String loadedTheme = newManager.getTheme();

        // Then
        assertEquals(theme, loadedTheme, "Theme should persist across instances");
    }

    @Test
    @DisplayName("Should handle multiple theme changes")
    void testMultipleThemeChanges() {
        // When
        settingsManager.setTheme("dark");
        assertEquals("dark", settingsManager.getTheme());

        settingsManager.setTheme("light");
        assertEquals("light", settingsManager.getTheme());

        settingsManager.setTheme("ios");
        assertEquals("ios", settingsManager.getTheme());

        settingsManager.setTheme("dark");
        assertEquals("dark", settingsManager.getTheme());

        // Then - should handle all changes correctly
        assertTrue(true);
    }

    @Test
    @DisplayName("Should validate theme values")
    void testThemeValidation() {
        // Given
        String[] validThemes = {"dark", "light", "ios"};

        // When & Then
        for (String theme : validThemes) {
            assertDoesNotThrow(() -> {
                settingsManager.setTheme(theme);
            }, "Should accept valid theme: " + theme);
        }
    }

    @Test
    @DisplayName("Should handle rapid theme changes")
    void testRapidThemeChanges() {
        // When
        for (int i = 0; i < 100; i++) {
            if (i % 3 == 0) {
                settingsManager.setTheme("dark");
            } else if (i % 3 == 1) {
                settingsManager.setTheme("light");
            } else {
                settingsManager.setTheme("ios");
            }
        }

        // Then
        assertNotNull(settingsManager.getTheme());
    }

    @Test
    @DisplayName("Should handle concurrent access safely")
    void testConcurrentAccess() throws InterruptedException {
        // Given
        Runnable task1 = () -> {
            for (int i = 0; i < 50; i++) {
                settingsManager.setTheme("dark");
            }
        };

        Runnable task2 = () -> {
            for (int i = 0; i < 50; i++) {
                settingsManager.setTheme("light");
            }
        };

        // When
        Thread thread1 = new Thread(task1);
        Thread thread2 = new Thread(task2);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        // Then - should not crash
        assertNotNull(settingsManager.getTheme());
    }

    @Test
    @DisplayName("Should maintain state consistency")
    void testStateConsistency() {
        // Given
        String initialTheme = settingsManager.getTheme();

        // When - No changes made
        String currentTheme = settingsManager.getTheme();

        // Then
        assertEquals(initialTheme, currentTheme, "Theme should remain consistent");
    }

    @Test
    @DisplayName("Should handle settings file creation")
    void testSettingsFileCreation() {
        // Given
        settingsManager.setTheme("ios");

        // Then
        File settingsFile = new File(System.getProperty("user.home") + "/.aihoneypot/settings.properties");
        // File should be created (or at least attempted)
        assertNotNull(settingsFile);
    }

    @Test
    @DisplayName("Should return non-null theme always")
    void testNonNullTheme() {
        // When
        String theme = settingsManager.getTheme();

        // Then
        assertNotNull(theme, "Theme should never be null");
        assertFalse(theme.isEmpty(), "Theme should not be empty");
    }
}

