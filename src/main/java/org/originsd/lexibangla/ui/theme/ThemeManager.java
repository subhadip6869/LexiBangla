package org.originsd.lexibangla.ui.theme;

import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import org.originsd.lexibangla.config.UserSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages active application theme (Light, Dark, System) and typography scaling.
 */
public final class ThemeManager {

    private static final Logger log = LoggerFactory.getLogger(ThemeManager.class);

    public enum Theme {
        LIGHT("/styles/light-theme.css"),
        DARK("/styles/dark-theme.css"),
        SYSTEM("/styles/light-theme.css"); // On Windows/Linux/macOS default to light, or detect OS dark mode if supported

        private final String cssPath;

        Theme(String cssPath) {
            this.cssPath = cssPath;
        }

        public String getCssPath() {
            return cssPath;
        }
    }

    private static final String MASTER_THEME_CSS = "/styles/lexibangla-theme.css";
    private static Theme currentTheme = Theme.LIGHT;
    private static int currentFontSizeDelta = 0;

    private ThemeManager() {}

    public static void applyTheme(Scene scene, UserSettings settings) {
        if (scene == null) return;

        Theme targetTheme = Theme.LIGHT;
        if (settings != null && settings.getTheme() != null) {
            try {
                targetTheme = Theme.valueOf(settings.getTheme().toUpperCase());
            } catch (IllegalArgumentException e) {
                targetTheme = Theme.LIGHT;
            }
        }

        currentTheme = targetTheme;
        currentFontSizeDelta = settings != null ? settings.getFontSizeDelta() : 0;

        scene.getStylesheets().clear();

        // 1. Add palette
        String palette = ThemeManager.class.getResource(currentTheme.getCssPath()).toExternalForm();
        scene.getStylesheets().add(palette);

        // 2. Add master styles
        String master = ThemeManager.class.getResource(MASTER_THEME_CSS).toExternalForm();
        scene.getStylesheets().add(master);

        // 3. Apply font size scaling inline on root
        int baseSize = 13 + currentFontSizeDelta;
        baseSize = Math.clamp(baseSize, 10, 24);
        scene.getRoot().setStyle("-fx-font-size: " + baseSize + "px;");

        log.info("Applied theme {} with font size {}px", currentTheme, baseSize);
    }

    public static void applyTheme(Dialog<?> dialog, UserSettings settings) {
        if (dialog == null) return;
        applyTheme(dialog.getDialogPane(), settings);
    }

    public static void applyTheme(DialogPane pane, UserSettings settings) {
        if (pane == null) return;

        Theme targetTheme = Theme.LIGHT;
        if (settings != null && settings.getTheme() != null) {
            try {
                targetTheme = Theme.valueOf(settings.getTheme().toUpperCase());
            } catch (IllegalArgumentException e) {
                targetTheme = Theme.LIGHT;
            }
        }

        pane.getStylesheets().clear();
        String palette = ThemeManager.class.getResource(targetTheme.getCssPath()).toExternalForm();
        pane.getStylesheets().add(palette);
        String master = ThemeManager.class.getResource(MASTER_THEME_CSS).toExternalForm();
        pane.getStylesheets().add(master);

        int baseSize = 13 + (settings != null ? settings.getFontSizeDelta() : 0);
        baseSize = Math.clamp(baseSize, 10, 24);
        pane.setStyle("-fx-font-size: " + baseSize + "px;");
    }

    public static Theme getCurrentTheme() {
        return currentTheme;
    }

    public static int getCurrentFontSizeDelta() {
        return currentFontSizeDelta;
    }
}
