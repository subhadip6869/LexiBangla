package org.originsd.lexibangla;

import javafx.application.Application;
import javafx.stage.Stage;
import org.originsd.lexibangla.db.HibernateUtil;
import org.originsd.lexibangla.ui.MainWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main JavaFX Application class for LexiBangla Desktop.
 */
public class App extends Application {

    private static final Logger log = LoggerFactory.getLogger(App.class);
    private MainWindow mainWindow;

    @Override
    public void start(Stage primaryStage) {
        log.info("Starting LexiBangla Desktop application UI");
        try {
            mainWindow = new MainWindow(primaryStage);
            mainWindow.show();
        } catch (Exception e) {
            log.error("Fatal error during application startup", e);
            throw e;
        }
    }

    @Override
    public void stop() {
        log.info("Stopping LexiBangla Desktop application");
        if (mainWindow != null) {
            mainWindow.shutdown();
        }
        HibernateUtil.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
