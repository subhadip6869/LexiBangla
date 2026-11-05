package org.originsd.lexibangla.ui.dialog;

import java.awt.Desktop;
import java.net.URI;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.originsd.lexibangla.config.AppConfig;
import org.originsd.lexibangla.config.BuildInfo;
import org.originsd.lexibangla.config.UserSettings;
import org.originsd.lexibangla.ui.theme.ThemeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * About LexiBangla Dialog with branding, version, and publisher information.
 */
public class AboutDialog {

    private static final Logger log = LoggerFactory.getLogger(AboutDialog.class);

    public static void show(Stage owner, UserSettings settings) {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("About LexiBangla Desktop");
        dialog.setResizable(false);

        VBox content = new VBox(12);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(24));
        content.getStyleClass().add("detail-card");

        // Approved Logo
        ImageView logoView = new ImageView();
        try {
            Image logo = new Image(AboutDialog.class.getResourceAsStream("/icons/lexibangla-logo.png"));
            logoView.setImage(logo);
            logoView.setFitWidth(110);
            logoView.setFitHeight(110);
            logoView.setPreserveRatio(true);
            logoView.setSmooth(true);
        } catch (Exception e) {
            log.error("Failed to load logo in AboutDialog", e);
        }

        Label nameLabel = new Label(AppConfig.APP_NAME);
        nameLabel.getStyleClass().add("welcome-title");
        nameLabel.setStyle("-fx-font-size: 20px;");

        Label versionLabel = new Label("Version " + BuildInfo.getVersion() + " (" + BuildInfo.getBuildTimestamp() + ")");
        versionLabel.getStyleClass().add("status-badge");

        Label taglineLabel = new Label(AppConfig.TAGLINE);
        taglineLabel.getStyleClass().add("app-tagline");
        taglineLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        Label descLabel = new Label(AppConfig.DESCRIPTION);
        descLabel.getStyleClass().add("welcome-description");

        VBox infoBox = new VBox(4);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setPadding(new Insets(8, 0, 8, 0));

        Label publisherLabel = new Label("Publisher: " + AppConfig.PUBLISHER);
        publisherLabel.setStyle("-fx-text-fill: -color-text-secondary;");

        Label devLabel = new Label("Developer: " + AppConfig.DEVELOPER);
        devLabel.setStyle("-fx-text-fill: -color-text-secondary;");

        Label locationLabel = new Label("Location: " + AppConfig.LOCATION);
        locationLabel.setStyle("-fx-text-fill: -color-text-muted; -fx-font-size: 11px;");

        HBox linksBox = new HBox(12);
        linksBox.setAlignment(Pos.CENTER);

        Hyperlink webLink = new Hyperlink(AppConfig.WEBSITE_URL);
        webLink.setOnAction(e -> openUrl(AppConfig.WEBSITE_URL));

        Hyperlink repoLink = new Hyperlink("GitHub Repository");
        repoLink.setOnAction(e -> openUrl(AppConfig.REPO_URL));

        linksBox.getChildren().addAll(webLink, new Label("•"), repoLink);

        Label copyrightLabel = new Label(AppConfig.COPYRIGHT);
        copyrightLabel.setStyle("-fx-text-fill: -color-text-muted; -fx-font-size: 11px;");

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("action-btn-primary");
        closeBtn.setOnAction(e -> dialog.close());

        infoBox.getChildren().addAll(publisherLabel, devLabel, locationLabel, linksBox, copyrightLabel);
        content.getChildren().addAll(logoView, nameLabel, versionLabel, taglineLabel, descLabel, infoBox, closeBtn);

        Scene scene = new Scene(content, 420, 480);
        ThemeManager.applyTheme(scene, settings);

        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private static void openUrl(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception e) {
            log.warn("Failed to open browser URL: {}", url, e);
        }
    }
}
