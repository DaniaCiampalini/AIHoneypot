package com.aihoneypot.gui.controller;

import com.aihoneypot.gui.util.SettingsManager;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

/**
 * Controller for Settings Dialog.
 */
public class SettingsController {

    @FXML private RadioButton darkThemeRadio;
    @FXML private RadioButton lightThemeRadio;
    @FXML private RadioButton iosThemeRadio;

    private SettingsManager settingsManager;
    private Stage dialogStage;

    public void setSettingsManager(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;

        // Set current theme selection
        String currentTheme = settingsManager.getTheme();
        if ("light".equals(currentTheme)) {
            lightThemeRadio.setSelected(true);
        } else if ("ios".equals(currentTheme)) {
            iosThemeRadio.setSelected(true);
        } else {
            darkThemeRadio.setSelected(true);
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void initialize() {
        // Group radio buttons
        ToggleGroup themeGroup = new ToggleGroup();
        darkThemeRadio.setToggleGroup(themeGroup);
        lightThemeRadio.setToggleGroup(themeGroup);
        iosThemeRadio.setToggleGroup(themeGroup);
    }

    @FXML
    private void handleSave() {
        if (settingsManager != null) {
            String selectedTheme;
            if (lightThemeRadio.isSelected()) {
                selectedTheme = "light";
            } else if (iosThemeRadio.isSelected()) {
                selectedTheme = "ios";
            } else {
                selectedTheme = "dark";
            }
            settingsManager.setTheme(selectedTheme);
        }

        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}

