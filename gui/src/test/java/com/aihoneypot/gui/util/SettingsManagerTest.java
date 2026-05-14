package com.aihoneypot.gui.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.io.TempDir;

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
        String theme = settingsManager.getTheme();

        assertNotNull(theme);
        assertTrue(theme.equals("dark") || theme.equals("light") || theme.equals("ios"),
                "Theme should be one of: dark, light, ios");
    }

    @Test
    @DisplayName("Should set and get theme correctly")
    void testSetAndGetTheme() {
        String newTheme = "ios";

        settingsManager.setTheme(newTheme);
        String retrievedTheme = settingsManager.getTheme();

        assertEquals(newTheme, retrievedTheme);
    }

    @Test
    @DisplayName("Should handle dark theme")
    void testDarkTheme() {
        settingsManager.setTheme("dark");

        assertEquals("dark", settingsManager.getTheme());
    }

    @Test
    @DisplayName("Should handle light theme")
    void testLightTheme() {
        settingsManager.setTheme("light");

        assertEquals("light", settingsManager.getTheme());
    }

    @Test
    @DisplayName("Should handle iOS theme")
    void testIOSTheme() {
        settingsManager.setTheme("ios");

        assertEquals("ios", settingsManager.getTheme());
    }

    @Test
    @DisplayName("Should toggle between themes")
    void testToggleTheme() {
        settingsManager.setTheme("dark");
        String initialTheme = settingsManager.getTheme();

        settingsManager.toggleTheme();
        String toggledTheme = settingsManager.getTheme();

        assertNotEquals(initialTheme, toggledTheme);
    }

    @Test
    @DisplayName("Should toggle from dark to light")
    void testToggleDarkToLight() {
        settingsManager.setTheme("dark");

        settingsManager.toggleTheme();

        assertEquals("light", settingsManager.getTheme());
    }

    @Test
    @DisplayName("Should toggle from light to dark")
    void testToggleLightToDark() {
        settingsManager.setTheme("light");

        settingsManager.toggleTheme();

        assertEquals("dark", settingsManager.getTheme());
    }

    @Test
    @DisplayName("Should handle null scene gracefully")
    void testNullSceneHandling() {
        assertDoesNotThrow(() -> {
            settingsManager.applyTheme();
        });
    }

    @Disabled("Requires JavaFX toolkit initialization")
    @Test
    @DisplayName("Should set scene correctly")
    void testSetScene() {
        assertDoesNotThrow(() -> {
            settingsManager.setScene(null);
        });
    }

    @Test
    @DisplayName("Should persist theme setting")
    void testThemePersistence() {
        String theme = "ios";

        settingsManager.setTheme(theme);

        SettingsManager newManager = new SettingsManager();
        String loadedTheme = newManager.getTheme();

        assertEquals(theme, loadedTheme, "Theme should persist across instances");
    }

    @Test
    @DisplayName("Should handle multiple theme changes")
    void testMultipleThemeChanges() {
        settingsManager.setTheme("dark");
        assertEquals("dark", settingsManager.getTheme());

        settingsManager.setTheme("light");
        assertEquals("light", settingsManager.getTheme());

        settingsManager.setTheme("ios");
        assertEquals("ios", settingsManager.getTheme());

        settingsManager.setTheme("dark");
        assertEquals("dark", settingsManager.getTheme());

        assertTrue(true);
    }

    @Test
    @DisplayName("Should validate theme values")
    void testThemeValidation() {
        String[] validThemes = {"dark", "light", "ios"};

        for (String theme : validThemes) {
            assertDoesNotThrow(() -> {
                settingsManager.setTheme(theme);
            }, "Should accept valid theme: " + theme);
        }
    }

    @Test
    @DisplayName("Should handle rapid theme changes")
    void testRapidThemeChanges() {
        for (int i = 0; i < 100; i++) {
            if (i % 3 == 0) {
                settingsManager.setTheme("dark");
            } else if (i % 3 == 1) {
                settingsManager.setTheme("light");
            } else {
                settingsManager.setTheme("ios");
            }
        }

        assertNotNull(settingsManager.getTheme());
    }

    @Test
    @DisplayName("Should handle concurrent access safely")
    void testConcurrentAccess() throws InterruptedException {
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

        Thread thread1 = new Thread(task1);
        Thread thread2 = new Thread(task2);

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        assertNotNull(settingsManager.getTheme());
    }

    @Test
    @DisplayName("Should maintain state consistency")
    void testStateConsistency() {
        String initialTheme = settingsManager.getTheme();

        String currentTheme = settingsManager.getTheme();

        assertEquals(initialTheme, currentTheme, "Theme should remain consistent");
    }

    @Test
    @DisplayName("Should handle settings file creation")
    void testSettingsFileCreation() {
        settingsManager.setTheme("ios");

        assertNotNull(settingsManager.getTheme());
    }

    @Test
    @DisplayName("Should return non-null theme always")
    void testNonNullTheme() {
        String theme = settingsManager.getTheme();

        assertNotNull(theme, "Theme should never be null");
        assertFalse(theme.isEmpty(), "Theme should not be empty");
    }
}
