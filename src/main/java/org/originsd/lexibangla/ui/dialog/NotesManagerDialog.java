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
import org.originsd.lexibangla.model.PersonalNote;
import org.originsd.lexibangla.ui.theme.ThemeManager;

/**
 * Dedicated manager interface for viewing, editing, and deleting personal notes.
 */
public class NotesManagerDialog {

    public static void show(Stage owner, UserDataRepository repository, UserSettings settings, Consumer<String> onSelectWord) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Personal Notes Manager");
        dialog.setWidth(680);
        dialog.setHeight(560);

        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.getStyleClass().add("detail-card");

        Label header = new Label("Personal Notes Manager");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: -color-primary;");

        TextField filterInput = new TextField();
        filterInput.setPromptText("Filter notes by word or content...");
        filterInput.getStyleClass().add("search-text-field");

        TableView<PersonalNote> table = new TableView<>();
        table.getStyleClass().add("results-list-view");
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<PersonalNote, String> wordCol = new TableColumn<>("Word");
        wordCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getWord()));
        wordCol.setPrefWidth(160);

        TableColumn<PersonalNote, String> contentCol = new TableColumn<>("Note Content");
        contentCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getContent()));
        contentCol.setPrefWidth(320);

        TableColumn<PersonalNote, String> dateCol = new TableColumn<>("Updated");
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUpdatedAt().toString().substring(0, 10)));
        dateCol.setPrefWidth(120);

        table.getColumns().addAll(wordCol, contentCol, dateCol);

        List<PersonalNote> allNotes = repository.getAllNotes();
        ObservableList<PersonalNote> masterData = FXCollections.observableArrayList(allNotes);
        FilteredList<PersonalNote> filteredData = new FilteredList<>(masterData, p -> true);

        filterInput.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(note -> {
                if (newVal == null || newVal.isBlank()) return true;
                String q = newVal.toLowerCase().trim();
                return note.getWord().toLowerCase().contains(q) || note.getContent().toLowerCase().contains(q);
            });
        });

        table.setItems(filteredData);

        // Edit note area below
        VBox editBox = new VBox(6);
        editBox.setPadding(new Insets(8, 0, 0, 0));
        Label editLabel = new Label("Edit Selected Note:");
        editLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: -color-text-secondary;");

        TextArea noteEditArea = new TextArea();
        noteEditArea.setPrefRowCount(3);
        noteEditArea.setWrapText(true);
        noteEditArea.getStyleClass().add("notes-text-area");
        noteEditArea.setDisable(true);

        editBox.getChildren().addAll(editLabel, noteEditArea);

        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Button saveEditBtn = new Button("Save Edit");
        saveEditBtn.getStyleClass().add("action-btn-primary");
        saveEditBtn.setDisable(true);

        Button viewWordBtn = new Button("View in Dictionary");
        viewWordBtn.getStyleClass().add("action-btn-secondary");
        viewWordBtn.setDisable(true);

        Button deleteBtn = new Button("Delete Note");
        deleteBtn.getStyleClass().add("action-btn-secondary");
        deleteBtn.setDisable(true);

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("action-btn-secondary");
        closeBtn.setOnAction(e -> dialog.close());

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> {
            boolean hasSel = selected != null;
            noteEditArea.setDisable(!hasSel);
            saveEditBtn.setDisable(!hasSel);
            viewWordBtn.setDisable(!hasSel);
            deleteBtn.setDisable(!hasSel);
            if (hasSel) {
                noteEditArea.setText(selected.getContent());
            } else {
                noteEditArea.clear();
            }
        });

        saveEditBtn.setOnAction(e -> {
            PersonalNote selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String newContent = noteEditArea.getText();
                PersonalNote updated = repository.saveOrUpdateNote(selected.getWordId(), selected.getWord(), newContent);
                if (updated != null) {
                    selected.setContent(newContent);
                    table.refresh();
                }
            }
        });

        viewWordBtn.setOnAction(e -> {
            PersonalNote selected = table.getSelectionModel().getSelectedItem();
            if (selected != null && onSelectWord != null) {
                onSelectWord.accept(selected.getWord());
                dialog.close();
            }
        });

        deleteBtn.setOnAction(e -> {
            PersonalNote selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                repository.deleteNote(selected.getId());
                masterData.remove(selected);
                noteEditArea.clear();
            }
        });

        btnBox.getChildren().addAll(deleteBtn, saveEditBtn, viewWordBtn, closeBtn);
        root.getChildren().addAll(header, filterInput, table, editBox, btnBox);

        Scene scene = new Scene(root);
        ThemeManager.applyTheme(scene, settings);

        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
