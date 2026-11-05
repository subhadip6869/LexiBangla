package org.originsd.lexibangla.ui.dialog;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.originsd.lexibangla.config.UserSettings;
import org.originsd.lexibangla.ui.theme.ThemeManager;

/**
 * Keyboard Shortcuts cheat-sheet dialog.
 */
public class KeyboardShortcutsDialog {

    public static void show(Stage owner, UserSettings settings) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Keyboard Shortcuts");
        dialog.setResizable(false);

        VBox root = new VBox(14);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("detail-card");

        Label header = new Label("Keyboard Shortcuts");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: -color-primary;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(8);

        addShortcut(grid, 0, "Ctrl / Cmd + F", "Focus Search Bar");
        addShortcut(grid, 1, "Ctrl / Cmd + L", "Clear & Focus Search");
        addShortcut(grid, 2, "Esc", "Clear Search or Close Dialog");
        addShortcut(grid, 3, "Ctrl / Cmd + C", "Copy Selected Word / Entry");
        addShortcut(grid, 4, "Ctrl / Cmd + D", "Add / Remove Favorite");
        addShortcut(grid, 5, "Ctrl / Cmd + H", "Open Search History");
        addShortcut(grid, 6, "Ctrl / Cmd + B", "Open Favorites Manager");
        addShortcut(grid, 7, "Ctrl / Cmd + ,", "Open Settings");
        addShortcut(grid, 8, "F1", "Open Documentation & Help");

        HBox btnBox = new HBox();
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("action-btn-primary");
        closeBtn.setOnAction(e -> dialog.close());
        btnBox.getChildren().add(closeBtn);

        root.getChildren().addAll(header, grid, btnBox);

        Scene scene = new Scene(root, 400, 400);
        ThemeManager.applyTheme(scene, settings);

        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private static void addShortcut(GridPane grid, int row, String shortcut, String description) {
        Label keyLabel = new Label(shortcut);
        keyLabel.getStyleClass().add("status-badge");
        keyLabel.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: -color-text-primary;");

        grid.add(keyLabel, 0, row);
        grid.add(descLabel, 1, row);
    }
}
