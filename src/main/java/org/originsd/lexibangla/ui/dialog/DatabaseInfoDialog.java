package org.originsd.lexibangla.ui.dialog;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.originsd.lexibangla.config.AppConfig;
import org.originsd.lexibangla.config.UserSettings;
import org.originsd.lexibangla.db.MasterDictionaryDao;
import org.originsd.lexibangla.db.UserDataRepository;
import org.originsd.lexibangla.ui.theme.ThemeManager;

/**
 * Displays comprehensive database information, record counts, and integrity status.
 */
public class DatabaseInfoDialog {

    public static void show(Stage owner, MasterDictionaryDao masterDao, UserDataRepository userRepo, UserSettings settings) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Database Information");
        dialog.setResizable(false);

        VBox root = new VBox(12);
        root.setPadding(new Insets(18));
        root.getStyleClass().add("detail-card");

        Label header = new Label("Database Architecture & Status");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: -color-primary;");

        // Master Dictionary Section
        Label masterTitle = new Label("1. Master Dictionary (Read-Only)");
        masterTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: -color-secondary-teal;");

        GridPane masterGrid = new GridPane();
        masterGrid.setHgap(12);
        masterGrid.setVgap(6);

        long count = masterDao.getTotalWordCount();
        masterGrid.add(createKeyLabel("Total Word Entries:"), 0, 0);
        Label countLabel = createValLabel(String.format("%,d words", count));
        masterGrid.add(countLabel, 1, 0);

        masterGrid.add(createKeyLabel("Access Engine:"), 0, 1);
        masterGrid.add(createValLabel("Direct SQLite JDBC (Prepared Statements)"), 1, 1);

        masterGrid.add(createKeyLabel("Access Mode:"), 0, 2);
        Label roLabel = createValLabel("Strictly Read-Only (Unmodified)");
        roLabel.setStyle("-fx-text-fill: -color-success; -fx-font-weight: bold;");
        masterGrid.add(roLabel, 1, 2);

        masterGrid.add(createKeyLabel("Database File:"), 0, 3);
        Label pathLabel = new Label(masterDao.getDatabasePath().toString());
        pathLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: -color-text-muted;");
        masterGrid.add(pathLabel, 1, 3);

        // User Data Section
        Label userTitle = new Label("2. User Database (Writable)");
        userTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: -color-secondary-teal;");

        GridPane userGrid = new GridPane();
        userGrid.setHgap(12);
        userGrid.setVgap(6);

        userGrid.add(createKeyLabel("Favorites Count:"), 0, 0);
        userGrid.add(createValLabel(String.valueOf(userRepo.getFavoriteCount())), 1, 0);

        userGrid.add(createKeyLabel("Personal Notes Count:"), 0, 1);
        userGrid.add(createValLabel(String.valueOf(userRepo.getNoteCount())), 1, 1);

        userGrid.add(createKeyLabel("Search History Count:"), 0, 2);
        userGrid.add(createValLabel(String.valueOf(userRepo.getSearchHistoryCount())), 1, 2);

        userGrid.add(createKeyLabel("Persistence Layer:"), 0, 3);
        userGrid.add(createValLabel("Hibernate ORM 6 + Jakarta Persistence (JPA)"), 1, 3);

        userGrid.add(createKeyLabel("Database Location:"), 0, 4);
        Label userPathLabel = new Label(AppConfig.getUserDatabasePath().toString());
        userPathLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: -color-text-muted;");
        userGrid.add(userPathLabel, 1, 4);

        HBox btnBox = new HBox();
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("action-btn-primary");
        closeBtn.setOnAction(e -> dialog.close());
        btnBox.getChildren().add(closeBtn);

        root.getChildren().addAll(header, masterTitle, masterGrid, new Separator(), userTitle, userGrid, btnBox);

        Scene scene = new Scene(root, 560, 480);
        ThemeManager.applyTheme(scene, settings);

        dialog.setScene(scene);
        dialog.sizeToScene();
        dialog.showAndWait();
    }

    private static Label createKeyLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("db-info-key");
        return label;
    }

    private static Label createValLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("db-info-val");
        return label;
    }
}
