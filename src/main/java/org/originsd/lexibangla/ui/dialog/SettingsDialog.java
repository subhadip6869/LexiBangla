package org.originsd.lexibangla.ui.dialog;

import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.originsd.lexibangla.config.UserSettings;
import org.originsd.lexibangla.ui.theme.ThemeManager;

/**
 * Settings Dialog for configuring application appearance and behavior.
 */
public class SettingsDialog {

    public static void show(Stage owner, UserSettings settings, Consumer<UserSettings> onSave) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("LexiBangla Settings");
        dialog.setResizable(false);

        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("detail-card");

        Label header = new Label("Preferences");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: -color-primary;");

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(12);
        grid.setPadding(new Insets(8, 0, 8, 0));

        // 1. Theme
        Label themeLabel = new Label("Appearance Theme:");
        ComboBox<String> themeBox = new ComboBox<>();
        themeBox.getItems().addAll("LIGHT", "DARK", "SYSTEM");
        themeBox.setValue(settings.getTheme() != null ? settings.getTheme() : "LIGHT");
        themeBox.getStyleClass().add("mode-selector");
        grid.add(themeLabel, 0, 0);
        grid.add(themeBox, 1, 0);

        // 2. Font Size Scaling
        Label fontLabel = new Label("Font Size Adjustment:");
        Spinner<Integer> fontSpinner = new Spinner<>(-4, 12, settings.getFontSizeDelta());
        fontSpinner.setPrefWidth(100);
        grid.add(fontLabel, 0, 1);
        grid.add(fontSpinner, 1, 1);

        // 3. Show Sidebar
        Label sidebarLabel = new Label("Show Navigation Sidebar:");
        CheckBox sidebarCheck = new CheckBox();
        sidebarCheck.setSelected(settings.isShowSidebar());
        grid.add(sidebarLabel, 0, 2);
        grid.add(sidebarCheck, 1, 2);

        // 4. Show Status Bar
        Label statusBarLabel = new Label("Show Status Bar:");
        CheckBox statusBarCheck = new CheckBox();
        statusBarCheck.setSelected(settings.isShowStatusBar());
        grid.add(statusBarLabel, 0, 3);
        grid.add(statusBarCheck, 1, 3);

        // 5. Max Search Results
        Label maxResultsLabel = new Label("Max Search Results:");
        ComboBox<Integer> maxResultsBox = new ComboBox<>();
        maxResultsBox.getItems().addAll(20, 40, 60, 100, 200);
        maxResultsBox.setValue(settings.getMaxSearchResults());
        grid.add(maxResultsLabel, 0, 4);
        grid.add(maxResultsBox, 1, 4);

        // 6. Search Debounce
        Label debounceLabel = new Label("Search Debounce (ms):");
        Spinner<Integer> debounceSpinner = new Spinner<>(50, 1000, settings.getSearchDebounceMs(), 50);
        debounceSpinner.setPrefWidth(100);
        grid.add(debounceLabel, 0, 5);
        grid.add(debounceSpinner, 1, 5);

        // 7. Show Welcome on Startup
        Label welcomeLabel = new Label("Show Welcome on Startup:");
        CheckBox welcomeCheck = new CheckBox();
        welcomeCheck.setSelected(settings.isShowWelcomeOnStartup());
        grid.add(welcomeLabel, 0, 6);
        grid.add(welcomeCheck, 1, 6);

        // 8. Auto Check Updates
        Label updateLabel = new Label("Automatically Check for Updates:");
        CheckBox updateCheck = new CheckBox();
        updateCheck.setSelected(settings.isAutoCheckUpdates());
        grid.add(updateLabel, 0, 7);
        grid.add(updateCheck, 1, 7);

        HBox btnBox = new HBox(12);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        btnBox.setPadding(new Insets(10, 0, 0, 0));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("action-btn-secondary");
        cancelBtn.setOnAction(e -> dialog.close());

        Button saveBtn = new Button("Save Settings");
        saveBtn.getStyleClass().add("action-btn-primary");
        saveBtn.setOnAction(e -> {
            settings.setTheme(themeBox.getValue());
            settings.setFontSizeDelta(fontSpinner.getValue());
            settings.setShowSidebar(sidebarCheck.isSelected());
            settings.setShowStatusBar(statusBarCheck.isSelected());
            settings.setMaxSearchResults(maxResultsBox.getValue());
            settings.setSearchDebounceMs(debounceSpinner.getValue());
            settings.setShowWelcomeOnStartup(welcomeCheck.isSelected());
            settings.setAutoCheckUpdates(updateCheck.isSelected());

            if (onSave != null) {
                onSave.accept(settings);
            }
            dialog.close();
        });

        btnBox.getChildren().addAll(cancelBtn, saveBtn);
        root.getChildren().addAll(header, grid, btnBox);

        Scene scene = new Scene(root, 440, 450);
        ThemeManager.applyTheme(scene, settings);

        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
