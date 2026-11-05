package org.originsd.lexibangla.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * User-configurable application preferences.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSettings {

    private String theme = "LIGHT"; // LIGHT, DARK, SYSTEM
    private int fontSizeDelta = 0; // -4 to +12
    private boolean showSidebar = true;
    private boolean showStatusBar = true;
    private boolean showWelcomeOnStartup = true;
    private int searchDebounceMs = 200;
    private int maxSearchResults = 60;
    private boolean autoCheckUpdates = true;

    public UserSettings() {}

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public int getFontSizeDelta() {
        return fontSizeDelta;
    }

    public void setFontSizeDelta(int fontSizeDelta) {
        this.fontSizeDelta = fontSizeDelta;
    }

    public boolean isShowSidebar() {
        return showSidebar;
    }

    public void setShowSidebar(boolean showSidebar) {
        this.showSidebar = showSidebar;
    }

    public boolean isShowStatusBar() {
        return showStatusBar;
    }

    public void setShowStatusBar(boolean showStatusBar) {
        this.showStatusBar = showStatusBar;
    }

    public boolean isShowWelcomeOnStartup() {
        return showWelcomeOnStartup;
    }

    public void setShowWelcomeOnStartup(boolean showWelcomeOnStartup) {
        this.showWelcomeOnStartup = showWelcomeOnStartup;
    }

    public int getSearchDebounceMs() {
        return searchDebounceMs;
    }

    public void setSearchDebounceMs(int searchDebounceMs) {
        this.searchDebounceMs = searchDebounceMs;
    }

    public int getMaxSearchResults() {
        return maxSearchResults;
    }

    public void setMaxSearchResults(int maxSearchResults) {
        this.maxSearchResults = maxSearchResults;
    }

    public boolean isAutoCheckUpdates() {
        return autoCheckUpdates;
    }

    public void setAutoCheckUpdates(boolean autoCheckUpdates) {
        this.autoCheckUpdates = autoCheckUpdates;
    }
}
