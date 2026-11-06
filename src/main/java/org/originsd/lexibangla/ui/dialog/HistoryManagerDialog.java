package org.originsd.lexibangla.ui.dialog;

import java.util.List;
import java.util.function.Consumer;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import org.originsd.lexibangla.model.SearchHistory;
import org.originsd.lexibangla.ui.theme.ThemeManager;

/**
 * Dedicated manager interface for viewing and clearing search history.
 */
public class HistoryManagerDialog {

    public static void show(Stage owner, UserDataRepository repository, UserSettings settings, Consumer<String> onSelectQuery) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Search History Manager");
        dialog.setWidth(550);
        dialog.setHeight(480);

        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.getStyleClass().add("detail-card");

        Label header = new Label("Search History");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: -color-primary;");

        TableView<SearchHistory> table = new TableView<>();
        table.getStyleClass().add("results-list-view");
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<SearchHistory, String> queryCol = new TableColumn<>("Search Query");
        queryCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getQuery()));
        queryCol.setPrefWidth(260);

        TableColumn<SearchHistory, String> dateCol = new TableColumn<>("Timestamp");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSearchTime().toString().replace('T', ' ').substring(0, 19)));
        dateCol.setPrefWidth(200);

        table.getColumns().addAll(queryCol, dateCol);

        List<SearchHistory> historyList = repository.getRecentSearches(200);
        ObservableList<SearchHistory> masterData = FXCollections.observableArrayList(historyList);
        table.setItems(masterData);

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Button searchAgainBtn = new Button("Search Query");
        searchAgainBtn.getStyleClass().add("action-btn-primary");
        searchAgainBtn.setDisable(true);

        Button clearAllBtn = new Button("Clear All History");
        clearAllBtn.getStyleClass().add("action-btn-secondary");

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("action-btn-secondary");
        closeBtn.setOnAction(e -> dialog.close());

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            searchAgainBtn.setDisable(selected == null);
        });

        searchAgainBtn.setOnAction(e -> {
            SearchHistory selected = table.getSelectionModel().getSelectedItem();
            if (selected != null && onSelectQuery != null) {
                onSelectQuery.accept(selected.getQuery());
                dialog.close();
            }
        });

        clearAllBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to clear your entire search history?", ButtonType.YES, ButtonType.NO);
            confirm.initOwner(dialog);
            ThemeManager.applyTheme(confirm, settings);
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    repository.clearSearchHistory();
                    masterData.clear();
                }
            });
        });

        table.setRowFactory(tv -> {
            TableRow<SearchHistory> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    SearchHistory rowData = row.getItem();
                    if (onSelectQuery != null) {
                        onSelectQuery.accept(rowData.getQuery());
                        dialog.close();
                    }
                }
            });
            return row;
        });

        btnBox.getChildren().addAll(clearAllBtn, searchAgainBtn, closeBtn);
        root.getChildren().addAll(header, table, btnBox);

        Scene scene = new Scene(root);
        ThemeManager.applyTheme(scene, settings);

        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
