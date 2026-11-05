package org.originsd.lexibangla.ui.dialog;

import java.util.List;
import java.util.function.Consumer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.originsd.lexibangla.config.UserSettings;
import org.originsd.lexibangla.db.UserDataRepository;
import org.originsd.lexibangla.model.Favorite;
import org.originsd.lexibangla.ui.theme.ThemeManager;

/**
 * Dedicated manager interface for reviewing and removing favorites.
 */
public class FavoritesManagerDialog {

    public static void show(Stage owner, UserDataRepository repository, UserSettings settings, Consumer<String> onSelectWord) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Favorites Manager");
        dialog.setWidth(550);
        dialog.setHeight(500);

        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.getStyleClass().add("detail-card");

        Label header = new Label("Favorites Manager");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: -color-primary;");

        TextField filterInput = new TextField();
        filterInput.setPromptText("Filter favorites...");
        filterInput.getStyleClass().add("search-text-field");

        TableView<Favorite> table = new TableView<>();
        table.getStyleClass().add("results-list-view");
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Favorite, String> wordCol = new TableColumn<>("Word");
        wordCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getWord()));
        wordCol.setPrefWidth(220);

        TableColumn<Favorite, String> dateCol = new TableColumn<>("Added On");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCreatedAt().toString().substring(0, 10)));
        dateCol.setPrefWidth(160);

        table.getColumns().addAll(wordCol, dateCol);

        List<Favorite> allFavs = repository.getAllFavorites();
        ObservableList<Favorite> masterData = FXCollections.observableArrayList(allFavs);
        FilteredList<Favorite> filteredData = new FilteredList<>(masterData, p -> true);

        filterInput.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(fav -> {
                if (newVal == null || newVal.isBlank()) return true;
                return fav.getWord().toLowerCase().contains(newVal.toLowerCase().trim());
            });
        });

        table.setItems(filteredData);

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Button openBtn = new Button("View in Dictionary");
        openBtn.getStyleClass().add("action-btn-primary");
        openBtn.setDisable(true);

        Button removeBtn = new Button("Remove");
        removeBtn.getStyleClass().add("action-btn-secondary");
        removeBtn.setDisable(true);

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("action-btn-secondary");
        closeBtn.setOnAction(e -> dialog.close());

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            boolean hasSel = selected != null;
            openBtn.setDisable(!hasSel);
            removeBtn.setDisable(!hasSel);
        });

        openBtn.setOnAction(e -> {
            Favorite selected = table.getSelectionModel().getSelectedItem();
            if (selected != null && onSelectWord != null) {
                onSelectWord.accept(selected.getWord());
                dialog.close();
            }
        });

        removeBtn.setOnAction(e -> {
            Favorite selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                repository.removeFavorite(selected.getWord());
                masterData.remove(selected);
            }
        });

        table.setRowFactory(tv -> {
            TableRow<Favorite> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Favorite rowData = row.getItem();
                    if (onSelectWord != null) {
                        onSelectWord.accept(rowData.getWord());
                        dialog.close();
                    }
                }
            });
            return row;
        });

        btnBox.getChildren().addAll(removeBtn, openBtn, closeBtn);
        root.getChildren().addAll(header, filterInput, table, btnBox);

        Scene scene = new Scene(root);
        ThemeManager.applyTheme(scene, settings);

        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
