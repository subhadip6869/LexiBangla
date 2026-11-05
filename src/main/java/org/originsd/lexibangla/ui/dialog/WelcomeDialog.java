package org.originsd.lexibangla.ui.dialog;

import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.originsd.lexibangla.config.AppConfig;
import org.originsd.lexibangla.config.UserSettings;
import org.originsd.lexibangla.ui.theme.ThemeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Polished welcome screen displayed on first launch.
 */
public class WelcomeDialog {

    private static final Logger log = LoggerFactory.getLogger(WelcomeDialog.class);

    public static void show(Stage owner, UserSettings settings, Consumer<UserSettings> onSettingsUpdated) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Welcome to " + AppConfig.APP_NAME);
        dialog.setResizable(false);

        VBox content = new VBox(16);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(32));
        content.getStyleClass().add("welcome-container");

        ImageView logoView = new ImageView();
        try {
            Image logo = new Image(WelcomeDialog.class.getResourceAsStream("/icons/lexibangla-logo.png"));
            logoView.setImage(logo);
            logoView.setFitWidth(140);
            logoView.setFitHeight(140);
            logoView.setPreserveRatio(true);
            logoView.setSmooth(true);
        } catch (Exception e) {
            log.error("Failed to load logo in WelcomeDialog", e);
        }

        Label titleLabel = new Label(AppConfig.APP_NAME);
        titleLabel.getStyleClass().add("welcome-title");

        Label taglineLabel = new Label("Words Connect Worlds.");
        taglineLabel.getStyleClass().add("welcome-tagline");

        Label descLabel = new Label("Discover the meaning of every word.");
        descLabel.getStyleClass().add("welcome-description");

        CheckBox dontShowAgain = new CheckBox("Open directly into dictionary next time");
        dontShowAgain.setSelected(true);
        dontShowAgain.setStyle("-fx-text-fill: -color-text-secondary; -fx-padding: 8 0 4 0;");

        Button openBtn = new Button("Open Dictionary");
        openBtn.getStyleClass().add("action-btn-primary");
        openBtn.setStyle("-fx-font-size: 15px; -fx-padding: 8 24 8 24;");
        openBtn.setOnAction(e -> {
            if (dontShowAgain.isSelected()) {
                settings.setShowWelcomeOnStartup(false);
                if (onSettingsUpdated != null) {
                    onSettingsUpdated.accept(settings);
                }
            }
            dialog.close();
        });

        content.getChildren().addAll(logoView, titleLabel, taglineLabel, descLabel, dontShowAgain, openBtn);

        Scene scene = new Scene(content, 480, 460);
        ThemeManager.applyTheme(scene, settings);

        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
